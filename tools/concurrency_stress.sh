#!/usr/bin/env bash
# Concurrency stress test for the JVM UAssetCLI drop-in.
#
# Reproduces the scenario that broke the C# (dotnet) UAssetCLI on Linux: 8 concurrent
# tojson/fromjson processes. Requires 100% success over repeated rounds. Each worker runs in its
# own cwd with its own copies of the asset (no shared state), like automod's per-table dirs.
#
# Usage: tools/concurrency_stress.sh <asset.uasset> <usmap> [rounds]
set -u

ASSET="${1:?usage: concurrency_stress.sh <asset.uasset> <usmap> [rounds]}"
USMAP="${2:?missing usmap}"
ROUNDS="${3:-5}"
JAR="$(cd "$(dirname "$0")/../uassetcli/build/libs" && pwd)/uassetcli.jar"
ENGINE_VER="VER_UE4_25"
WORK="$(mktemp -d /tmp/opencode/concurrency.XXXXXX)"
trap 'rm -rf "$WORK"' EXIT

ASSET_DIR="$(dirname "$ASSET")"
if [ ! -f "$JAR" ]; then echo "missing jar: $JAR"; exit 2; fi
if [ ! -f "$ASSET" ]; then echo "missing asset: $ASSET"; exit 2; fi

# reference outputs (serial, authoritative)
mkdir -p "$WORK/ref"
cp "$ASSET" "$WORK/ref/"
[ -f "${ASSET%.uasset}.uexp" ] && cp "${ASSET%.uasset}.uexp" "$WORK/ref/"
(cd "$WORK/ref" && java -jar "$JAR" tojson "$(basename "$ASSET")" ref.json "$ENGINE_VER" "$USMAP")
[ -f "$WORK/ref/ref.json" ] || { echo "reference tojson failed"; exit 1; }
(cd "$WORK/ref" && java -jar "$JAR" fromjson ref.json ref_out.uasset "$USMAP")

fail=0
for round in $(seq 1 "$ROUNDS"); do
  mkdir -p "$WORK/r$round"
  for i in $(seq 0 7); do
    mkdir -p "$WORK/r$round/w$i"
    cp "$ASSET" "$WORK/r$round/w$i/"
    [ -f "${ASSET%.uasset}.uexp" ] && cp "${ASSET%.uasset}.uexp" "$WORK/r$round/w$i/"
  done
  # 8 concurrent tojson
  for i in $(seq 0 7); do
    ( cd "$WORK/r$round/w$i" && java -jar "$JAR" tojson "$(basename "$ASSET")" out.json "$ENGINE_VER" "$USMAP" && cmp -s out.json "$WORK/ref/ref.json" ) &
  done
  wait || { echo "round $round: tojson failed"; fail=1; }
  # 8 concurrent fromjson
  for i in $(seq 0 7); do
    ( cd "$WORK/r$round/w$i" && java -jar "$JAR" fromjson out.json out.uasset "$USMAP" && cmp -s out.uasset "$WORK/ref/ref_out.uasset" && cmp -s out.uexp "$WORK/ref/ref_out.uexp" ) &
  done
  wait || { echo "round $round: fromjson failed"; fail=1; }
  echo "round $round: OK"
done

if [ "$fail" -eq 0 ]; then echo "CONCURRENCY STRESS: PASS ($ROUNDS rounds x 8 parallel x tojson+fromjson)"; else echo "CONCURRENCY STRESS: FAIL"; exit 1; fi
