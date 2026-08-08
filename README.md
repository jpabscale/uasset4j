# uasset4j

[![CI](https://github.com/jpabscale/uasset4j/actions/workflows/ci.yml/badge.svg)](https://github.com/jpabscale/uasset4j/actions/workflows/ci.yml)
[![JitPack](https://jitpack.io/v/jpabscale/uasset4j.svg)](https://jitpack.io/#jpabscale/uasset4j)

A **Kotlin/JVM port of [UAssetAPI](https://github.com/atenfyr/UAssetAPI)** — an Unreal Engine
asset parser/serializer. It is an almost one-to-one, **statement-parallel** port, fully done by an
LLM: the Kotlin source mirrors the C# source file-for-file, statement-for-statement, so upstream
UAssetAPI changes stay cheap to adopt. It also includes **curve support** derived from
[CUE4Parse](https://github.com/FabianFG/CUE4Parse) (Apache-2.0), providing dedicated types for
`FRichCurve`, `FSimpleCurve`, `FCompressedRichCurve`, `UCurveTable`, and related classes that
UAssetAPI lacks.

> **Performance** — the in-JVM pipeline avoids per-asset subprocess round-trips: automod's
> `.demo.sb` (Stellar Blade, same machine, same patches) ran in **52s** with uasset4j vs **2:38**
> with the C# `UAssetCLI.dll` subprocess. The [complexity-parity rule](#porting-discipline) keeps it
> that way: a functionally-identical but asymptotically slower rewrite is rejected.

> **Ported UAssetAPI commit: `33ef77e`**
>
> All code is ported from this exact upstream tree. The differential oracle — the built C#
> [UAssetCLI](https://github.com/atenfyr/UAssetCLI) used to verify parity — must match this commit.
> When a newer upstream tip is adopted, bump this sha everywhere (see
> [Keeping up with UAssetAPI](#keeping-up-with-uassetapi)).

## What it is

- **`uassetapi`** — the ported library (Kotlin/JVM, `com.github.jpabscale.uasset4j.*`): binary
  readers/writers, `UnrealTypes`, property types, export types, Kismet bytecode, usmap unversioned
  properties, and the JSON (de)serialization layer.
- **`uassetcli`** — a **cross-platform, JVM-based drop-in replacement** for the C#
  [UAssetCLI](https://github.com/atenfyr/UAssetCLI). A fat/uber jar (`uassetcli.jar`) that runs
  anywhere a JVM runs (Linux, macOS, Windows). It is the CLI that tools fork today (e.g. automod's
  `tojson`/`fromjson` pipeline) and doubles as the parity harness.
- **Curve support** — dedicated curve types derived from
  [CUE4Parse](https://github.com/FabianFG/CUE4Parse) (Apache-2.0, EXC-002), pinned to
  commit `e9f24e0`:
  `FRichCurve`/`FSimpleCurve` (with `Eval`), `FCompressedRichCurve` (decompression via
  `ConverterMap` adapters), `UCurveTable` (`CurveTableExport`), `UCurveVector`,
  `UCurveLinearColor`, `UCurveLinearColorAtlas`, `FCurveMetaData`, `FKeyHandle`. The existing
  UAssetAPI-ported curve files (`FRichCurveKey`, `RichCurveKeyPropertyData`) remain unchanged.
- **Ported tests** — `UAssetAPI.Tests` (MSTest) ported to JUnit 5, run against the same binary
  corpus, plus a byte-for-byte JSON oracle test, plus curve model unit tests.

### Scope

In scope: `UAssetAPI/UAssetAPI/**`, `UAssetAPI.Tests/**`, and `UAssetCLI/**` (the CLI becomes the
JVM drop-in). Out of scope:

- **`UAssetGUI/**`** — the WinForms app; irrelevant to automod.
- **`Pak/`** — dropped entirely. The C# `RePak.cs` is P/Invoke to a native pak library, the only
  native dependency upstream; automod already uses retoc/repak for containers. Dropping it keeps
  the JVM port at **zero native deps** (Jackson + qyntrax only), which is also what keeps a future
  GraalVM native-image clean.
- **`UAssetAPI.Benchmark`** — deferred.

## Why

UAssetCLI (the C# binary) is the build-time asset tool used by the modding pipeline. As a .NET
assembly it runs on any platform with the .NET runtime installed, but it is not reliable across
platforms: it fails to serialize uassets on macOS, it crashed under Linux parallel builds, and
.NET's behavior is not identical across platforms (e.g. directory enumeration order). Both the
C# and JVM binaries need a runtime (`.NET` vs a JVM 21+), but the JVM port behaves
consistently on every platform and, as a library, can be loaded in-process by the JVM tooling
(e.g. automod) that currently subprocesses the C# binary.

### Config safety

The port never writes shared state. `UAGConfig` is ported **read-only** (mappings lookup only, no
`Save()`), and mappings are resolved per call (an explicit `mappingsName`/path argument first, then
the read-only lookup). The API and CLI therefore cannot corrupt the shared config directory that
broke Linux parallel runs upstream.

## Building locally

Requirements:

- **JDK 25** for compilation. The build uses a JDK 25 toolchain; if it isn't installed, the
  [foojay resolver](https://github.com/gradle/foojay-resolver-convention) auto-downloads it on
  first build. The produced bytecode targets **JVM 21**, so the jars run on any JVM 21+.
- **`curl` or `wget`** on first build only: the Gradle wrapper jar is not committed; `gradlew`
  fetches it from the pinned Gradle 9.6.1 release and verifies its SHA-256 (from
  `gradle.properties`). Windows uses the bundled `curl.exe` + `certutil`.
- Network access to Maven Central (dependencies) and `services.gradle.org` (Gradle distribution).

Build, test, and produce the CLI fat jar:

```
./gradlew build                  # compile + run the JUnit suite (ported AssetUnitTests + oracle tests)
./gradlew :uassetcli:shadowJar   # build the drop-in CLI: uassetcli/build/libs/uassetcli.jar
```

Run the CLI:

```
java -jar uassetcli/build/libs/uassetcli.jar
# Usage: UAssetCLI [ fromjson <source> <destination> [mappings name]
#                  | tojson <source> <destination> <engine version> [mappings name] ]
```

## Porting discipline

The port follows a strict **parity contract** so that a ported file is a mechanical translation,
not a rewrite:

- **Statement-level parity is a hard rule.** Every C# statement, branch, call, and loop appears, in
  order, in the Kotlin method — translated only through the mappings in
  [`docs/mapping.md`](docs/mapping.md). *Functional* parity (producing the same output) is
  necessary but never sufficient; a restructured port is rejected and reworked to restore the C#
  shape. The reason: only a statement-parallel port keeps upstream diffs cheap. When UAssetAPI
  changes a method, the port mirrors that diff mechanically and the porting cost stays proportional
  to the size of the upstream change — whereas a functionally-equivalent but restructured port has
  to be re-read and re-derived every time.
- **Names and paths mirror C#.** `UAssetAPI/UAssetAPI/<path>` → `uassetapi/src/main/kotlin/<path>`;
  class/method/field/property names are preserved verbatim (with documented exceptions like
  `Equals`→`equals`).
- **`tools/audit_parity.py`** audits every file marked `ported` in
  [`docs/port-tracker.md`](docs/port-tracker.md): every C# public member must exist on the Kotlin
  side. Green at milestone close.
- **Approved parity exceptions.** A deliberate, user-approved divergence from the oracle (e.g. the
  `.usmap.gz` mappings format, which the C# UAssetAPI rejects) may be recorded — and only there —
  in [`docs/parity-exceptions.json`](docs/parity-exceptions.json). Each entry carries approval
  metadata (`approved_by`, `approved_on`, `reason`) and can only be added/modified/revoked on an
  explicit user instruction. The exception code is wrapped in `//@parity:on <id>` /
  `//@parity:off <id>` comment markers; `tools/audit_parity.py` verifies the markers are balanced
  and reference registry ids. `tools/sweep.py` buckets approved divergences as `EXC` (always
  printed, never silently skipped) and `--check-stale` reports exceptions whose assets no longer
  diverge so they can be revoked. Anything not in the registry is still a defect and must be fixed
  at the C# source.
- **Algorithmic-complexity parity.** A Kotlin method that is functionally equivalent but
  asymptotically slower than its C# counterpart is a parity violation even when every asset
  MATCHES and member names align. Reviewers compare per-construct complexity (lookup vs
  `.toSet()` rebuilds, loop nesting, guard/invariant checks) and reject slower rewrites.

See [`docs/mapping.md`](docs/mapping.md) for the full translation table and
[`AGENTS.md`](AGENTS.md) for the agent/review rules that enforce this discipline.

## Testing against UAssetCLI

Functional parity is enforced by differential testing against the pinned C# oracle:

- **Corpus sweep** — `tools/sweep.py` runs the JVM `UAssetCLI.jar` and the C# `UAssetCLI.dll`
  `tojson` on every `.uasset` in the test corpus (369 assets) and byte-compares the output, in
  parallel:
  ```
  python3 tools/sweep.py uassetcli/build/libs/uassetcli.jar \
      <dir-containing-UAssetCLI.dll> uassetapi/src/test/resources/testassets
  ```
  Target state: `MATCH 369, DIFF 0, JVMERR 0, CSERR 0, BOTHERR 0`. The sweep isolates each asset
  in a per-worker dir (the C# oracle fails on the same file opened concurrently) and decrypts
  encrypted fixtures (e.g. Ace Combat 7) with a Python `AC7Decrypt` port before the parity CLIs,
  mirroring the C# test. `BOTHERR` would be cases where *both* implementations fail identically
  (oracle limitations, not port bugs). `EXC` counts approved divergences from
  `docs/parity-exceptions.json`.
- **Differential** — `tools/differential.sh` byte-compares `tojson` on a given asset set.
- **Round-trips** — `tojson → fromjson → tojson` must be stable; where C# is lossy (e.g.
  ClassExport CDO collapse), the JVM reproduces the identical lossy output.
- **JUnit 5** — the ported `AssetUnitTests`, the JSON oracle test, and curve model tests run via
  `./gradlew test`.
- **CUE4Parse curve parity** — `tools/cue4parse_parity.py` verifies that every C# curve type
  member in the CUE4Parse reference source has a corresponding Kotlin implementation or approved
  architecture exception (EXC-002):
  ```
  python3 tools/cue4parse_parity.py
  ```
  Target state: `PARITY AUDIT: GREEN`.
- **Concurrency stress** — `tools/concurrency_stress.sh` runs parallel `tojson`/`fromjson`
  processes repeatedly and requires 100% pass: the exact scenario that broke the C# binary on
  Linux, proving the port fixes it.
- **Cross-platform CI** — GitHub Actions runs the full suite on Linux/macOS/Windows; the macOS
  lane is the proof that the original macOS block is solved.

The oracle binary is automod's built `UAssetCLI.dll` (net10.0) at the pinned commit, run with
`~/.dotnet/dotnet`.

## Keeping up with UAssetAPI

Because the port is statement-parallel, adopting a newer upstream release is mechanical:

1. **Re-pin** — update the pinned sha in this README, the `docs/mapping.md` header, and
   `docs/port-tracker.md`.
2. **Diff upstream** — `git -C <UAssetCLI clone> diff <old-sha>..<new-sha> -- UAssetAPI/UAssetAPI`
   and port each changed C# file. Each file is a localized, statement-level translation; most diffs
   are small (new properties, version-gated branches, new export/property types).
3. **Update the mapping** — any new C# construct gets a `docs/mapping.md` entry before it is ported.
4. **Regenerate the oracle** — rebuild the C# `UAssetCLI` from the new pin and re-run
   `tools/sweep.py` until `DIFF 0`.
5. **Update the tests** — fold in any new `UAssetAPI.Tests` cases into the ported JUnit suite.

The port is pinned to `UAssetAPI-33ef77e`; the submodule lives at
`/home/jpabscale/Repositories/UAssetCLI/UAssetAPI` in the automod-vscode workspace.

## Publishing

- **Git tag** a release as `<uassetapi-sha>.<cue4parse-sha>.<ref>` (e.g. `33ef77e.e9f24e0.1`)
  to publish — both upstream pins are encoded in the tag and `<ref>` is a monotonic
  release counter. The Gradle project version is derived from the tag (`git describe`),
  so the artifact version always matches the ref it was built from.
- **JitPack** builds the library on demand from the tag. The consuming coordinate is
  `com.github.jpabscale:uasset4j:<tag>` (JitPack maps the group by user, artifact by repo) —
  automod:
  ```
  //> using repository https://jitpack.io
  //> using dep com.github.jpabscale:uasset4j:33ef77e.e9f24e0.1
  ```
- **GitHub Actions** (`.github/workflows/ci.yml`) builds and runs the full test suite on
  every push/PR, and on a tag push creates a GitHub Release whose asset is the
  **fat jar** `uassetcli.jar` (the drop-in CLI replacement).
- The port compiles to **JVM 21 bytecode** (regardless of the build JDK), so the jar
  runs on any JVM 21+ — including automod's Zulu JDK 25 and scala-cli's default JVM.

## Layout

```
uassetapi/src/main/kotlin/com/github/jpabscale/uasset4j/   # ported library (mirrors UAssetAPI/UAssetAPI)
uassetapi/src/main/kotlin/com/github/jpabscale/uasset4j/curves/  # CUE4Parse derivative curve types (EXC-002)
uassetapi/src/test/                                     # ported JUnit tests + oracle fixtures + curve tests
uassetcli/                                              # thin CLI wrapper → UAssetCLI.jar (fat jar)
tools/                                                  # parity harnesses (sweep.py, differential.sh, audit_parity.py, cue4parse_parity.py)
docs/mapping.md                                         # C# → Kotlin translation contract
docs/port-tracker.md                                    # per-file port status + oracle notes
```

## License

The ported code is derived from [UAssetAPI](https://github.com/atenfyr/UAssetAPI) (MIT,
Copyright (c) 2020-2026 atenfyr). The repo ships [LICENSE](LICENSE) with UAssetAPI's original MIT
text and notice, plus its own copyright line, and every ported file carries an attribution header
(`Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr`). New parts (the CLI wrapper,
`Zstd.kt`, the JSON/API layer) are also MIT.

The curve support under `uassetapi/src/main/kotlin/com/github/jpabscale/uasset4j/curves/` and
`exporttypes/CurveTableExport.kt` is derivative work of
[CUE4Parse](https://github.com/FabianFG/CUE4Parse) (Apache-2.0, Copyright (c) FabianFG and
contributors). These files carry Apache-2.0 attribution headers and `//@parity:on EXC-002` /
`//@parity:off EXC-002` markers. See [NOTICE](NOTICE) and
[docs/parity-exceptions.json](docs/parity-exceptions.json) for details.

**The test corpus is NOT covered by MIT.** The 813 `TestAssets` fixtures are derived from games,
testing-only, and remain the IP of their rights holders; `uassetapi/src/test/resources/testassets/NOTICE.md`
documents this. They are internal test fixtures, never redistributed with the published artifact.
