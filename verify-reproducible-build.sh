#!/usr/bin/env bash
# Verifies that the Maven build is reproducible by building twice and comparing checksums.
# Exits with 0 if the build is reproducible, 1 otherwise.

set -euo pipefail

MVN="${MVN:-mvn}"
BUILD_ARGS=(-B -q clean package)

TMPDIR_1=$(mktemp -d)
TMPDIR_2=$(mktemp -d)
trap 'rm -rf "$TMPDIR_1" "$TMPDIR_2"' EXIT

echo "=== First build ==="
"$MVN" "${BUILD_ARGS[@]}"

while IFS= read -r jar; do
  name="${jar#./}"
  name="${name//\//_}"
  cp "$jar" "$TMPDIR_1/$name"
done < <(find . -path '*/target/*.jar' ! -path '*/target/it/*' | sort)

echo "=== Second build ==="
"$MVN" "${BUILD_ARGS[@]}"

while IFS= read -r jar; do
  name="${jar#./}"
  name="${name//\//_}"
  cp "$jar" "$TMPDIR_2/$name"
done < <(find . -path '*/target/*.jar' ! -path '*/target/it/*' | sort)

echo "=== Comparing artifacts ==="
PASS=true
shopt -s nullglob
for jar1 in "$TMPDIR_1"/*.jar; do
  name=$(basename "$jar1")
  jar2="$TMPDIR_2/$name"
  if [ ! -f "$jar2" ]; then
    echo "MISSING in second build: $name"
    PASS=false
    continue
  fi
  sum1=$(sha256sum "$jar1" | awk '{print $1}')
  sum2=$(sha256sum "$jar2" | awk '{print $1}')
  if [ "$sum1" = "$sum2" ]; then
    echo "OK  $name  ($sum1)"
  else
    echo "MISMATCH $name"
    echo "  build 1: $sum1"
    echo "  build 2: $sum2"
    PASS=false
  fi
done

if $PASS; then
  echo ""
  echo "SUCCESS: build is reproducible."
  exit 0
else
  echo ""
  echo "FAILURE: build is not reproducible."
  exit 1
fi
