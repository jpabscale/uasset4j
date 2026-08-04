# AGENTS.md — rules for AI agents and reviewers on this repo

This file is the single source of truth for agent behavior. It is written to be read before doing
any work here. The one-line summary: **the port is a statement-parallel translation of the pinned
UAssetAPI C# source — never a rewrite.** The `curves/` package is an Apache-2.0 derivative of
CUE4Parse, governed by EXC-002.

## Hard rules

### 1. Statement-level parity is the contract (NOT just functional parity)

- Every C# statement, branch, call, and loop in a ported method must appear, **in the same order**,
  in the Kotlin method — translated only through [`docs/mapping.md`](docs/mapping.md).
- "Functional parity" (producing the same output) is **necessary but never sufficient**. A patch
  that changes the C# structure — inlining a base-class method as a local override, rewriting a
  `switch` over a different value shape, collapsing a helper, reordering statements — is a
  **parity violation even if tests pass**.
- When the literal translation is awkward in Kotlin, the *first* resort is a new `docs/mapping.md`
  entry (so it becomes a canonical pattern), not a bespoke rewrite.
- Agents porting code must be able to point at the exact C# statement each Kotlin statement mirrors.
- **This rule applies to *fixes*, not just initial ports.** When parity breaks, the fix must restore
  the C# shape — the root cause is a divergence from the C# source, so repair it there. Do not
  "patch around" a functional failure (inline an inherited base method as a local override, reshape
  a `switch`/`when`, or otherwise restructure) to make the output match; that trades functional
  parity for code-parity debt and is rejected the same way a restructured port is.

**Verbatim rule to include in every porting/parity task prompt:**

> Statement-level parity is a hard rule. Every C# statement must appear, in order, in the Kotlin
> port, translated only through docs/mapping.md. Functional parity is necessary but never
> sufficient — do not rewrite the C# structure to make it work. When the literal translation is
> awkward, add a canonical mapping entry to docs/mapping.md first.

### 2. Establish the baseline before making changes

Before changing anything, record and report:

- `git status --short` and `git diff --stat` (the tree may hold uncommitted work — build on it,
  never revert it without asking).
- The failing repro output (e.g. `tools/sweep.py --only <asset>` lines, or the exact test failure).
- The reference "already passing" checks, and re-verify them after every change:
  - `python3 tools/sweep.py uassetcli/build/libs/uassetcli.jar <csdir> uassetapi/src/test/resources/testassets --only TestUE5_4` → `MATCH 31 DIFF 0` (`EXC` may be non-zero only for approved exceptions in `docs/parity-exceptions.json`)
  - SoA differential: `tools/differential.sh` against the `SandsOfAura_1.01.25.usmap` assets.
  - `python3 tools/audit_parity.py` → `PARITY AUDIT: GREEN`.
  - `python3 tools/cue4parse_parity.py` → `PARITY AUDIT: GREEN`.
  - `./gradlew build` → green.

A fix that doesn't show its baseline (before→after) is incomplete. Regressions (a previously-MATCH
asset turning DIFF) must be caught and fixed before the task is considered done.

## Conventions that must not be violated

- **NO explanatory comments** in ported Kotlin source files. The attribution header at the top of
  each ported file is fine; do not add prose comments. (The JSON layer, CLI, tools, and tests may
  carry doc comments where they explain non-obvious parity behavior — this is the documented
  exception in `docs/mapping.md`.)
- **CUE4Parse derivative files** (`curves/`, `exporttypes/CurveTableExport.kt`) are Apache-2.0
  licensed (EXC-002). They carry `//@parity:on EXC-002` / `//@parity:off EXC-002` markers and
  attribution headers. These files are NOT statement-parallel UAssetAPI ports — they are new code
  derived from CUE4Parse. Review them against CUE4Parse's source, not UAssetAPI. Run
  `tools/cue4parse_parity.py` to verify C# member coverage.
- **Exceptional regions** (`//@parity:on`/`//@parity:off` blocks) must be reviewed whenever the
  files they wrap are changed. Reviewers should verify the region's exception is still valid and
  the markers are balanced.
- **`$type` strings** in the JSON layer MUST stay `UAssetAPI.<Ns>.<Class>, UAssetAPI` — never
  rewrite them to the Kotlin package.
- **No new dependencies** without explicit approval. The zstd seam is the only native/third-party
  entry point; `Pak/` is dropped (zero native deps).
- **Do not change binary Read/Write behavior** unless fixing a genuine parity bug — and if you do,
  re-run the full sweep to prove no regression.
- **Approved parity exceptions live ONLY in `docs/parity-exceptions.json`.** This is the sole
  place a deliberate, user-approved divergence may be recorded (e.g. a `.usmap.gz` extension the
  C# oracle rejects). **Agents must never add, modify, revoke, or reorder an exception entry on
  their own — only an explicit user instruction may change this file.** Without an entry there,
  a divergence is a defect and must be fixed at the C# source. The tools (`tools/sweep.py`,
  `tools/audit_parity.py`) bucket approved items as `EXC` — always printed, never silently
  skipped — and   `tools/sweep.py --check-stale` reports exceptions whose assets no longer diverge
  so they can be revoked.
- **Approved exception code is marked in-source.** Every approved divergence must wrap its
  ported code region in `//@parity:on <exc-id>` ... `//@parity:off <exc-id>` comment markers
  (balanced, non-overlapping, in the same file, with an id that exists in
  `docs/parity-exceptions.json`). An unmarked divergence — even one covered by a registry entry
  — is a parity violation; the marker is what ties the code to its approval.
- **Do not commit** unless the task explicitly says to. Only `uasset4j` may be pushed; automod
  may be committed to for integration testing but never pushed. UAssetGUI is out of scope.

## Review checklist (for reviewers / reviewers-as-agents)

Reject a ported file if ANY of:

1. A Kotlin method can't be diffed statement-by-statement against its C# source (order, branch
   shape, call sites differ) — even if the output is byte-identical.
2. A C# construct was solved without a corresponding `docs/mapping.md` entry (or the entry was
   added after the fact rather than before).
3. `tools/audit_parity.py` reports a new mismatch.
4. The port touches a file marked `ported` in `docs/port-tracker.md` without updating the tracker.
5. New public members/classes don't mirror the C# names (`Equals`→`equals`, `GetHashCode`→
   `hashCode`, etc. are the documented exceptions).
6. `docs/parity-exceptions.json` was modified without an explicit user instruction for that change.
7. A ported file contains `//@parity:on`/`//@parity:off` markers that are unbalanced, nested,
   duplicated, or reference an id not present in `docs/parity-exceptions.json` — or a divergence
   exists in code without any markers at all.
8. **Algorithmic complexity diverges from the C# source.** A Kotlin method that is functionally
   equivalent but asymptotically slower than its C# counterpart is a parity violation, even if
   every corpus asset MATCHES and the member names align. Compare the complexity of each hot
   construct pair: collection operations (dictionary/index lookup vs `.toSet()`/`.contains` on a
   list), loops (nested vs single-pass), and guard/invariant checks (a cheap `size == 0` check vs
   a full `keys == list.toSet()` rebuild comparison per call). A functionally-identical rewrite
   that trades O(1)/O(n) for O(n)/O(n²) per call — like the `FixNameMapLookupIfNeeded` guard that
   rebuilt a `toSet()` comparison on every name-map access — must be rejected and restored to the
   C# shape.
9. **CUE4Parse derivative files** have not been verified with `tools/cue4parse_parity.py`.
   Run the checker and confirm GREEN before accepting.
10. **Exceptional regions** have not been reviewed when the wrapped file changed. Reviewers must
    verify `//@parity:on`/`//@parity:off` markers are balanced, reference valid EXC ids, and the
    underlying exception is still valid.

Acceptance bar for a parity task: statement-level review done, algorithmic-complexity review done
(each method's asymptotic behavior matches its C# source — no functionally-identical-but-slower
rewrites), `audit_parity.py` GREEN, `cue4parse_parity.py` GREEN, `./gradlew build` green, and the
corpus sweep at `MATCH 369 DIFF 0` (or the target stated in the task) with no regressions.

For CUE4Parse derivative files (`curves/`), the acceptance bar is: `cue4parse_parity.py` GREEN,
`./gradlew build` green, `@Test` methods pass, and the pre-existing corpus sweep unchanged
(derivative code does not alter the UAssetAPI-ported property path).

## Campaign insights

Lessons learned while getting the corpus to full parity — treat as operational guidance.

- **The oracle has known limitations; recognize them, don't fight them.** The corpus sweep's
  `BOTHERR` bucket is cases where *both* the JVM and the C# oracle fail identically — the C#
  binary itself crashes or throws (core dumps on many Blueprint/Class assets, package-level
  compression, `NullReferenceException` on some `fromjson` round-trips). These are oracle
  limitations, **not port bugs**: matching the C# exception is the correct outcome. Only a JVM
  failure where the C# succeeds (`JVMERR`, or a `DIFF`) is a real defect. Do not "fix" an asset
  that is a verified identical both-sides failure. Two oracle quirks are worked around in
  `tools/sweep.py`: XOR-encrypted fixtures (e.g. Ace Combat 7) are decrypted with a Python
  `AC7Decrypt` port before the parity CLIs, mirroring the C# test; and the C# oracle fails on the
  same file opened concurrently, so each asset is copied into a per-worker dir.
- **The unversioned round-trip bug is pre-existing.** Both UAssetAPI (C#) and uasset4j lose the
  2-byte FUnversionedHeader fragment on JSON→uasset round-trip for unversioned assets. This causes
  the re-read to fall to `RawExport`. This is NOT caused by the curve code and affects ALL
  unversioned assets. Do not flag it as a curve regression.
- **Delegate self-contained parity units to a fresh-context agent with its own test loop.** The
  corpus-fix campaign worked best when a single root-cause group (e.g. "Kismet `$type` JSON",
  "export type dispatch") was handed to one agent that reproduced, fixed at the source, rebuilt the
  jar, and re-ran its own sweep until green. Bundling many unrelated DIFFs into one agent's context
  or chaining them through the delegator's context exhausts it and slows iteration. A one-agent
  task is still worth spawning for a self-contained unit with its own verify loop.
- **Report honestly when stuck.** Prefer a report that says exactly which items are done/remaining
  and why, over a completed-sounding summary that hides a dead end. A fix that cannot point at its
  root cause in the C# source is not done.

## Reading order (context onboarding)

1. `README.md` — overview, pinned UAssetAPI sha, how the port tracks upstream.
2. `docs/mapping.md` — the C#→Kotlin translation contract (consult before any port).
3. `docs/port-tracker.md` — which files are ported, deferred, and how to regenerate oracle data.
4. This file — agent rules (always).
5. `tools/sweep.py` + `tools/differential.sh` — how functional parity is verified.
6. `tools/cue4parse_parity.py` — how CUE4Parse curve parity is verified.
6. `tools/cue4parse_parity.py` — how CUE4Parse curve parity is verified.

## Key paths

- Pinned C# source (READ-ONLY reference): `/home/jpabscale/Repositories/UAssetCLI/UAssetAPI/UAssetAPI/`
- CUE4Parse source (READ-ONLY reference): `/tmp/automod/CUE4Parse/CUE4Parse/`
- Oracle binary: `/home/jpabscale/Repositories/automod-vscode/automod/tools/UAssetCLI/UAssetCLI.dll`
  (net10.0, run with `~/.dotnet/dotnet`); JVM jar: `uassetcli/build/libs/uassetcli.jar`.
- Corpus: `uassetapi/src/test/resources/testassets/` (mirrors `UAssetAPI.Tests/TestAssets/`).
- Parity tools: `tools/sweep.py` (parallel corpus differential), `tools/differential.sh`,
  `tools/audit_parity.py` (UAssetAPI member-name audit), `tools/cue4parse_parity.py`
  (CUE4Parse curve-type audit), `tools/concurrency_stress.sh`.
- Curve code: `uassetapi/src/main/kotlin/com/github/jpabscale/uasset4j/curves/` (Apache-2.0
  derivatives), `exporttypes/CurveTableExport.kt`.
