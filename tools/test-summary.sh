#!/usr/bin/env bash
# Summarise JUnit XML results: total tests, failures, errors, skipped.
cd "$(dirname "$0")/.." || exit 1
t=0; f=0; e=0; s=0
for x in build/test-results/test/*.xml; do
  read -r a b c d < <(grep -oE '<testsuite [^>]*' "$x" | sed -E 's/.*tests="([0-9]+)".*skipped="([0-9]+)".*failures="([0-9]+)".*errors="([0-9]+)".*/\1 \2 \3 \4/')
  t=$((t+a)); s=$((s+b)); f=$((f+c)); e=$((e+d))
done
echo "tests=$t failures=$f errors=$e skipped=$s"
[ "$f" -eq 0 ] && [ "$e" -eq 0 ]
