#!/usr/bin/env python3
"""Parallel corpus sweep: run the JVM UAssetCLI.jar and the C# UAssetCLI.dll `tojson` on every
.uasset in the corpus and byte-compare the output.

Each asset runs in its own thread (subprocesses release the GIL); both CLIs are invoked with
absolute paths, so no temp-dir copying is needed. Results are aggregated deterministically
(sorted by asset path).

Engine version per directory prefix: TestUE5_* -> VER_UE5_1, everything else -> VER_UE4_26
(both CLIs get the SAME arg; versioned assets self-report via header).
Mappings: companion .usmap in the same dir, else the nearest .usmap in the asset's dir chain.

Usage: tools/sweep.py <jar> <csharp-uassetcli-dir> <corpus-root> [--jobs N] [--limit N]
                      [--match-list FILE] [--only ASSET_SUBSTR]
"""

import argparse
import json
import os
import subprocess
import sys
import tempfile
from concurrent.futures import ThreadPoolExecutor, as_completed
from pathlib import Path

from parity_exceptions import exempt, load_registry

CS_UASSETCLI = "UAssetCLI.dll"


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
    return "VER_UE5_1" if rel.startswith("TestUE5_") else "VER_UE4_26"


def run_one(args):
    rel, asset, usmap, ver, jar, csdir, dotnet, workdir = args
    d = Path(workdir)
    d.mkdir(parents=True, exist_ok=True)
    jvm_json, cs_json = d / "jvm.json", d / "cs.json"
    usmap_args = [str(usmap)] if usmap else []

    jvm = subprocess.run(
        ["java", "-jar", jar, "tojson", str(asset), str(jvm_json), ver, *usmap_args],
        capture_output=True,
    )
    cs = subprocess.run(
        [dotnet, str(Path(csdir) / CS_UASSETCLI), "tojson", str(asset), str(cs_json), ver, *usmap_args],
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
