#!/usr/bin/env bash
# Differential: JVM UAssetCLI.jar vs C# UAssetCLI.dll (automod's oracle) — tojson byte-identical.
# Usage: tools/differential.sh <jar> <csharp-uassetcli-dir> <engineVersion> <usmap> <asset.uasset> [more assets...]
set -u
JAR="$(realpath "${1:?jar}")"; CSDIR="$(realpath "${2:?csharp dir}")"; VER="${3:?engine ver}"; USMAP="$(realpath "${4:?usmap}")"
shift 4
WORK="$(mktemp -d /tmp/opencode/diff.XXXXXX)"; trap 'rm -rf "$WORK"' EXIT
fail=0
for asset in "$@"; do
  name="$(basename "${asset%.uasset}")"
  mkdir -p "$WORK/$name"
  cp "$asset" "$WORK/$name/"; [ -f "${asset%.uasset}.uexp" ] && cp "${asset%.uasset}.uexp" "$WORK/$name/"
  (cd "$WORK/$name" && java -jar "$JAR" tojson "$(basename "$asset")" jvm.json "$VER" "$USMAP") || { echo "FAIL(jvm) $name"; fail=1; continue; }
  (cd "$WORK/$name" && ~/.dotnet/dotnet "$CSDIR/UAssetCLI.dll" tojson "$(basename "$asset")" cs.json "$VER" "$USMAP") >/dev/null 2>&1 || { echo "FAIL(cs) $name"; fail=1; continue; }
  if cmp -s "$WORK/$name/jvm.json" "$WORK/$name/cs.json"; then
    echo "MATCH $name"
  else
    echo "DIFF $name"; fail=1
  fi
done
[ "$fail" -eq 0 ] && echo "DIFFERENTIAL: PASS" || { echo "DIFFERENTIAL: FAIL"; exit 1; }
