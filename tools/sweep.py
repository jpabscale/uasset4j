#!/usr/bin/env python3
"""Parallel corpus sweep: run the JVM UAssetCLI.jar and the C# UAssetCLI.dll `tojson` on every
.uasset in the corpus and byte-compare the output.

Each asset runs in its own thread (subprocesses release the GIL); both CLIs are invoked with
absolute paths, so no temp-dir copying is needed. Results are aggregated deterministically
(sorted by asset path).

Engine version per directory prefix: mirrors the pinned C# UAssetAPI test suite
(UAssetAPI.Tests/AssetUnitTests.cs). Unversioned packages carry no version in the header, so the
CLI arg is what drives header parsing — both CLIs get the SAME arg; versioned assets self-report.
Mappings: companion .usmap in the same dir, else the nearest .usmap in the asset's dir chain.

Usage: tools/sweep.py <jar> <csharp-uassetcli-dir> <corpus-root> [--jobs N] [--limit N]
                      [--match-list FILE] [--only ASSET_SUBSTR]
"""

import argparse
import json
import os
import shutil
import subprocess
import sys
import tempfile
from concurrent.futures import ThreadPoolExecutor, as_completed
from pathlib import Path

from parity_exceptions import exempt, load_registry

CS_UASSETCLI = "UAssetCLI.dll"

# Ace Combat 7 encrypted-asset magic (0x37454341 LE). These assets are XOR-encrypted; the C# test
# suite decrypts them in-place with UAssetAPI.AC7Decrypt before parsing, so neither CLI can read the
# raw bytes. We mirror that here: the sweep decrypts the worker-local copy (and its .uexp) with the
# same algorithm before running the parity CLIs. The 222208-byte key comes from UAssetAPI's
# Resources/AC7Key.bin (ported below), so we produce the identical decrypted bytes the C# test uses.
ACE7_MAGIC = b"\x41\x43\x45\x37"  # 0x37454341 little-endian

_AC7_KEY_PATH = Path(__file__).resolve().parent.parent / "uassetapi" / "src" / "main" / "resources" / "AC7Key.bin"
_AC7_KEY: bytes | None = None


def _ac7_key() -> bytes:
    global _AC7_KEY
    if _AC7_KEY is None:
        if not _AC7_KEY_PATH.exists():
            raise SystemExit(
                f"AC7Key.bin missing at {_AC7_KEY_PATH} — it is not committed; fetch it from the "
                "pinned UAssetAPI tarball (UAssetAPI/Resources/AC7Key.bin), e.g. the CI corpus step",
            )
        with open(_AC7_KEY_PATH, "rb") as f:
            _AC7_KEY = f.read()
    return _AC7_KEY


def _ac7_name_key(fname: str) -> int:
    num = 0
    for ch in fname.upper():
        num2 = ord(ch) & 0xFF
        num ^= num2
        num2 = (num * 8) & 0xFFFFFFFF
        num2 ^= num
        num3 = (num + num) & 0xFFFFFFFF
        num2 = (~num2) & 0xFFFFFFFF
        num2 = (num2 >> 7) & 1
        num = num2 | num3
    return num


def _ac7_pkey(nkey: int, dataoffset: int) -> tuple[int, int]:
    # Port of UAssetAPI.AC7XorKey.CalcPKeyFromNKey (C# long/BigInteger semantics).
    num = (nkey * 7) & 0xFFFFFFFF
    num += dataoffset
    big = 5440514381186227205
    big2 = big * num
    num2 = big2 >> 70
    num2 &= 0xFFFFFFFFFFFFFFFF
    if num2 >= (1 << 63):
        num2 -= (1 << 64)
    num3 = num2 >> 63
    num2 += num3
    num3 = num2 * 217
    num -= num3
    pk1 = num & 0xFFFFFFFF
    num4 = (nkey * 11) & 0xFFFFFFFF
    num4 += dataoffset
    num4 &= 0x3FF
    pk2 = num4 & 0xFFFFFFFF
    return pk1, pk2


def _ac7_xor_through(data: bytearray, pk1: int, pk2: int, start: int = 4) -> tuple[int, int]:
    key = _ac7_key()
    for i in range(start, len(data)):
        data[i] = (data[i] ^ key[pk1 * 1024 + pk2] ^ 0x77) & 0xFF
        pk1 += 1
        pk2 += 1
        if pk1 >= 217:
            pk1 = 0
        if pk2 >= 1024:
            pk2 = 0
    return pk1, pk2


def ac7_decrypt(local_asset: Path, d: Path, jar: str) -> Path:
    """Decrypt an ACE7 asset (+ sibling .uexp) in place, mirroring UAssetAPI.AC7Decrypt.Decrypt.

    The C# decryptor advances a single AC7XorKey across BOTH the .uasset and the .uexp (the uexp
    continues from the uasset's exhausted key state). Returns the path to the decrypted .uasset
    (a sibling .uexp is written next to it).
    """
    import struct

    name = local_asset.stem
    pk1, pk2 = _ac7_pkey(_ac7_name_key(name), 4)
    out = d / ("dec_" + local_asset.name)

    ua = bytearray(local_asset.read_bytes())
    pk1, pk2 = _ac7_xor_through(ua, pk1, pk2, start=4)
    ua[0:4] = struct.pack("<I", 0x9E2A83C1)  # UASSET_MAGIC
    out.write_bytes(bytes(ua))

    uexp = local_asset.with_suffix(".uexp")
    if uexp.exists():
        # Continue from the key state left after the .uasset (the C# decryptor shares one AC7XorKey
        # across both files). The uexp is xored across ALL bytes (no magic skip).
        ue = bytearray(uexp.read_bytes())
        _ac7_xor_through(ue, pk1, pk2, start=0)
        ue[-4:] = struct.pack("<I", 0x9E2A83C1)
        (d / ("dec_" + uexp.name)).write_bytes(bytes(ue))

    return out


def find_usmap(asset_dir: Path, cache: dict) -> str | None:
    """Nearest .usmap walking up from asset_dir (cached per directory)."""
    d = asset_dir
    while True:
        if d in cache:
            return cache[d]
        found = sorted(d.glob("*.usmap")) if d.is_dir() else []
        cache[d] = found[0] if found else None
        if found:
            return found[0]
        if d == d.parent:
            return None
        d = d.parent


def engine_for(rel: str) -> str:
    """Engine version for a corpus asset path, mirroring the pinned C# UAssetAPI test suite.

    Unversioned packages (PKG_UnversionedProperties) carry no version in the header; the CLI arg is
    what selects ObjectVersion, so the per-game/per-folder version from AssetUnitTests.cs is the
    source of truth. A wrong version misparses the header identically on both CLIs (silent BOTHERR).
    """
    # Per-asset overrides first (TestJson is a mixed folder: versioned UE4 assets and unversioned
    # UE5 assets side by side; the C# test pins each individually).
    if rel in _ENGINE_VERSION_BY_ASSET:
        return _ENGINE_VERSION_BY_ASSET[rel]
    # Longest-prefix match against the authoritative C# test subsections.
    for prefix, version in _ENGINE_VERSION_BY_PREFIX:
        if rel == prefix or rel.startswith(prefix + "/"):
            return version
    return "VER_UE4_26"


# Per-asset engine versions for mixed folders (from UAssetAPI.Tests/AssetUnitTests.cs TestJson()).
_ENGINE_VERSION_BY_ASSET: dict[str, str] = {
    # Unversioned UE5 assets in TestJson; the versioned UE4 assets default to VER_UE4_26.
    "TestJson/BlinkerLight_01.uasset": "VER_UE5_1",
    "TestJson/FrontDomeLight_2m.uasset": "VER_UE5_1",
    "TestJson/ReverseLight_01.uasset": "VER_UE5_1",
    "TestJson/TaliLight_01.uasset": "VER_UE5_1",
    "TestJson/MTVehicleBaseBP.uasset": "VER_UE5_1",
    "TestJson/Atlas_6x4_Semi.uasset": "VER_UE5_1",
}


# (dir prefix, engine version) pairs from UAssetAPI.Tests/AssetUnitTests.cs, longest first.
_ENGINE_VERSION_BY_PREFIX: list[tuple[str, str]] = [
    ("TestManyAssets/SN2", "VER_UE5_6"),
    ("TestManyAssets/Bellwright", "VER_UE5_6"),
    ("TestUE5_7", "VER_UE5_7"),
    ("TestUE5_6", "VER_UE5_6"),
    ("TestUE5_5", "VER_UE5_5"),
    ("TestUE5_4", "VER_UE5_4"),
    ("TestUE5_3", "VER_UE5_3"),
    ("TestUE5_1", "VER_UE5_1"),
    ("TestEditorUE5_7", "VER_UE5_7"),
    ("TestManyAssets/Clay", "VER_UE5_1"),
    ("TestManyAssets/Palia", "VER_UE5_1"),
    ("TestManyAssets/F1Manager2023", "VER_UE5_1"),
    ("TestManyAssets/Palworld", "VER_UE5_1"),
    ("TestManyAssets/LiesOfP", "VER_UE4_27"),
    # UE4 fallbacks (default catch-all is VER_UE4_26, listed for clarity):
    ("TestACE7", "VER_UE4_18"),
    ("TestManyAssets/Biodigital", "VER_UE4_14"),
    ("TestManyAssets/SnakePass", "VER_UE4_14"),
    ("TestManyAssets/Tekken", "VER_UE4_14"),
    ("TestManyAssets/MidAir", "VER_UE4_17"),
    ("TestManyAssets/MutantYearZero", "VER_UE4_17"),
    ("TestManyAssets/Bloodstained", "VER_UE4_18"),
    ("TestManyAssets/BurningDaylight", "VER_UE4_18"),
    ("TestManyAssets/CodeVein", "VER_UE4_18"),
    ("TestManyAssets/Liminal", "VER_UE4_18"),
    ("TestManyAssets/ToTheCore", "VER_UE4_18"),
    ("TestManyAssets/TheBeastInside", "VER_UE4_19"),
    ("TestManyAssets/TheOccupation", "VER_UE4_19"),
    ("TestManyAssets/Astroneer", "VER_UE4_23"),
    ("TestManyAssets/StarlitSeason", "VER_UE4_24"),
    ("TestManyAssets/MISC_426", "VER_UE4_26"),
    ("TestManyAssets/VERSIONED", "UNKNOWN"),
    ("TestJson", "VER_UE4_26"),
]


def run_one(args):
    rel, asset, usmap, ver, jar, csdir, dotnet, workdir = args
    d = Path(workdir)
    d.mkdir(parents=True, exist_ok=True)
    jvm_json, cs_json = d / "jvm.json", d / "cs.json"

    # Copy the asset (+ sibling .uexp) and usmap into this worker's private dir. The C# oracle
    # opens .uasset with a non-shared file handle; when many workers read the SAME file concurrently,
    # .NET throws IOException ("being used by another process") and the oracle either crashes or
    # degrades exports to RawExport, producing a spurious intermittent DIFF. Isolating inputs per
    # worker makes the sweep deterministic under parallelism.
    local_asset = d / asset.name
    shutil.copy2(asset, local_asset)
    uexp = asset.with_suffix(".uexp")
    if uexp.exists():
        shutil.copy2(uexp, d / uexp.name)
    local_usmap = None
    if usmap is not None:
        local_usmap = d / usmap.name
        shutil.copy2(usmap, local_usmap)
    usmap_args = [str(local_usmap)] if local_usmap else []

    # Ace Combat 7 assets are XOR-encrypted (magic 0x37454341). The C# test suite decrypts them
    # in-place (UAssetAPI.AC7Decrypt) before parsing, so neither CLI can read the raw bytes. Detect
    # the magic on the worker-local copy and decrypt it (and its .uexp) before the parity CLIs,
    # mirroring the C# test flow so both sides compare the same decrypted bytes.
    try:
        is_ace7 = local_asset.read_bytes()[:4] == ACE7_MAGIC
    except OSError:
        is_ace7 = False
    if is_ace7:
        local_asset = ac7_decrypt(local_asset, d, jar)

    jvm = subprocess.run(
        ["java", "-jar", jar, "tojson", str(local_asset), str(jvm_json), ver, *usmap_args],
        capture_output=True,
    )
    cs = subprocess.run(
        [dotnet, str(Path(csdir) / CS_UASSETCLI), "tojson", str(local_asset), str(cs_json), ver, *usmap_args],
        capture_output=True,
    )

    jvm_ok, cs_ok = jvm.returncode == 0, cs.returncode == 0
    if not jvm_ok and not cs_ok:
        verdict = "BOTHERR"
    elif not jvm_ok:
        verdict = "JVMERR"
    elif not cs_ok:
        verdict = "CSERR"
    elif jvm_json.read_bytes() == cs_json.read_bytes():
        verdict = "MATCH"
    else:
        verdict = "DIFF"
    return rel, verdict


def main():
    ap = argparse.ArgumentParser(description="Parallel JVM-vs-C# tojson parity sweep")
    ap.add_argument("jar", help="path to uassetcli.jar")
    ap.add_argument("csharp_dir", help="directory containing UAssetCLI.dll")
    ap.add_argument("corpus", help="corpus root (testassets)")
    ap.add_argument("--jobs", type=int, default=os.cpu_count() or 8)
    ap.add_argument("--limit", type=int, default=0)
    ap.add_argument("--match-list", default=None, help="file of rel paths to SKIP")
    ap.add_argument("--only", default=None, help="only assets whose rel path contains this substring")
    ap.add_argument("--keep", action="store_true", help="keep per-asset work dirs")
    ap.add_argument(
        "--check-stale",
        action="store_true",
        help="report approved exceptions that no longer diverge (for revocation); "
        "requires --exceptions and runs with the registry ignored",
    )
    ap.add_argument(
        "--exceptions",
        default=None,
        help="path to docs/parity-exceptions.json (default: the repo registry)",
    )
    args = ap.parse_args()

    # Approved-divergence registry. When present (and NOT in --check-stale mode), matching
    # non-MATCH assets are bucketed EXC instead of DIFF/JVMERR/CSERR/BOTHERR. In --check-stale
    # mode the registry is still loaded for scope matching, but NOT applied to verdicts, so
    # every divergence is real; we then report exceptions whose assets now all MATCH.
    exceptions = load_registry(args.exceptions)

    jar = os.path.realpath(args.jar)
    csdir = os.path.realpath(args.csharp_dir)
    corpus = Path(args.corpus).resolve()
    dotnet = os.environ.get("DOTNET", os.path.expanduser("~/.dotnet/dotnet"))

    matchlist = set()
    if args.match_list:
        with open(args.match_list) as f:
            matchlist = {line.strip() for line in f}

    # collect assets (only .uasset that have a sibling .uexp)
    assets = sorted(p for p in corpus.rglob("*.uasset") if p.with_suffix(".uexp").exists())
    if args.only:
        assets = [a for a in assets if args.only in str(a.relative_to(corpus))]
    if args.limit:
        assets = assets[: args.limit]

    usmap_cache: dict = {}
    workroot = Path(tempfile.mkdtemp(prefix="sweep.", dir="/tmp/opencode"))
    jobs = []
    for i, asset in enumerate(assets):
        rel = str(asset.relative_to(corpus))
        if rel in matchlist:
            continue
        ver = engine_for(rel)
        usmap = find_usmap(asset.parent, usmap_cache)
        jobs.append((rel, asset, usmap, ver, jar, csdir, dotnet, workroot / f"w{i}"))

    counts = {"MATCH": 0, "DIFF": 0, "JVMERR": 0, "CSERR": 0, "BOTHERR": 0, "EXC": 0}
    rows = []
    with ThreadPoolExecutor(max_workers=args.jobs) as ex:
        futures = [ex.submit(run_one, j) for j in jobs]
        for fut in as_completed(futures):
            rel, verdict = fut.result()
            if not args.check_stale and verdict != "MATCH" and exempt(exceptions, "sweep", rel):
                verdict = "EXC"
            counts[verdict] += 1
            rows.append((rel, verdict))

    for rel, verdict in sorted(rows):
        if verdict != "MATCH":
            print(f"{verdict} {rel}")

    total = len(rows)
    print("=" * 50)
    print(
        f"TOTAL {total}  MATCH {counts['MATCH']}  DIFF {counts['DIFF']}  "
        f"JVMERR {counts['JVMERR']}  CSERR {counts['CSERR']}  BOTHERR {counts['BOTHERR']}  "
        f"EXC {counts['EXC']}  SKIP {len(assets) - total}"
    )
    if args.check_stale:
        # With exceptions NOT applied, every non-MATCH is a real divergence. An approved
        # exception is stale when its scoped asset(s) all MATCH — the divergence it was
        # approved for is gone, so it should be revoked.
        verdicts = {rel: v for rel, v in rows}
        scoped = {rel for rel, _ in rows if exempt(exceptions, "sweep", rel)}
        stale = sorted(rel for rel in scoped if verdicts[rel] == "MATCH")
        if stale:
            print(f"STALE-EXC {len(stale)} approved exceptions whose assets no longer diverge:")
            for rel in stale:
                print(f"  {rel}")
            print("  -> review and revoke these in docs/parity-exceptions.json")
        else:
            print("STALE-EXC 0 (all approved exceptions still diverge)")
    if not args.keep:
        import shutil

        shutil.rmtree(workroot, ignore_errors=True)
    return 1 if counts["DIFF"] or counts["JVMERR"] or counts["CSERR"] else 0


if __name__ == "__main__":
    sys.exit(main())
