#!/usr/bin/env bash
# Static mixin audit: checks that every mixin target class, and every method/field
# named in @Inject/@Redirect/@ModifyArg/@Shadow/@Accessor/@Invoker, exists in the
# Yarn-named Minecraft jar or the Loom-remapped AltoClef/Baritone jars.
# Usage: tools/audit-mixins.sh   (run from repo root after ./gradlew build)
set -u
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
JAVAP="${JAVA_HOME:-/c/Program Files/Microsoft/jdk-17.0.16.8-hotspot}/bin/javap"
MC=$(ls "$HOME"/.gradle/caches/fabric-loom/minecraftMaven/net/minecraft/minecraft-merged/1.18.2-net.fabricmc.yarn.1_18_2.1.18.2+build.4-v2/minecraft-merged-1.18.2-*.jar | head -1)
MCI=$(ls "$HOME"/.gradle/caches/fabric-loom/minecraftMaven/net/minecraft/minecraft-merged-intermediary/1.18.2-*/minecraft-merged-intermediary-1.18.2-*.jar | head -1)
AC=$(ls "$ROOT"/.gradle/loom-cache/remapped_mods/*/unspecified/altoclef-0.4.0/*/altoclef-*.jar | head -1)
BA=$(ls "$ROOT"/.gradle/loom-cache/remapped_mods/*/unspecified/baritone*/*/baritone*.jar | head -1)
SEP=":"
case "$(uname -s)" in
  MINGW*|MSYS*|CYGWIN*) SEP=";"; MC=$(cygpath -w "$MC"); MCI=$(cygpath -w "$MCI"); AC=$(cygpath -w "$AC"); BA=$(cygpath -w "$BA");;
esac
# Intermediary Minecraft is included because mixins into AltoClef (remap = false) name MC members the way
# AltoClef's bytecode does: net/minecraft/class_XXXX;method_XXXX.
CP="$MC$SEP$MCI$SEP$AC$SEP$BA"
MIXDIR="$ROOT/src/main/java/altoclefplus/mixin"
miss=0
checked=0

has_member() { # $1=javap output, $2=name
  echo "$1" | grep -qE "[ .\$]$2( |;|\()"
}

while IFS= read -r -d "" f; do
  target=$(grep -oE '@Mixin\((value *= *)?[A-Za-z0-9_.]+\.class' "$f" | head -1 | sed -E 's/@Mixin\((value *= *)?//; s/\.class$//')
  fq=""
  if [ -n "$target" ]; then
    simple="${target%%.*}"
    rest="${target#"$simple"}"
    imp=$(grep -E "^import [a-zA-Z0-9_.]+\.${simple};" "$f" | head -1 | sed -E 's/^import //; s/;$//')
    if [ -n "$imp" ]; then fq="${imp}${rest//./\$}"; else fq="$target"; fi
  else
    fq=$(grep -oE '@Mixin\(targets *= *"[^"]+"' "$f" | head -1 | sed -E 's/.*"([^"]+)"/\1/')
  fi
  if [ -z "$fq" ]; then echo "MISS no-target $f"; miss=$((miss+1)); continue; fi
  out=$("$JAVAP" -p -cp "$CP" "$fq" 2>&1)
  checked=$((checked+1))
  if echo "$out" | grep -q "class not found"; then
    echo "MISS class $fq  ($f)"; miss=$((miss+1)); continue
  fi
  for m in $(grep -oE 'method *= *\{?"[^"]+"' "$f" | sed -E 's/.*"([^"(]+).*/\1/' | sort -u); do
    checked=$((checked+1))
    if [ "$m" = "<init>" ]; then continue; fi
    has_member "$out" "$m" || { echo "MISS method $fq#$m  ($f)"; miss=$((miss+1)); }
  done
  # @Shadow members: declaration on the same line or the next non-annotation line
  for m in $(awk '/@Shadow/ {pending=1} pending { line=$0; gsub(/@[A-Za-z]+(\([^)]*\))?/, "", line); if (line ~ /[;(=]/) { print line; pending=0 } }' "$f" | grep -oE '[A-Za-z_][A-Za-z0-9_]*\s*(\(|;|=)' | head -n 1000 | sed -E 's/\s*[(;=]$//' | sort -u); do
    checked=$((checked+1))
    has_member "$out" "$m" || { echo "MISS shadow $fq#$m  ($f)"; miss=$((miss+1)); }
  done
  # @Accessor("name") / @Invoker("name")
  for m in $(grep -oE '@(Accessor|Invoker)\("[^"]+"' "$f" | sed -E 's/.*"([^"]+)"/\1/' | sort -u); do
    checked=$((checked+1))
    has_member "$out" "$m" || { echo "MISS accessor $fq#$m  ($f)"; miss=$((miss+1)); }
  done
  # @At(target = "Lowner;name(...)")
  for t in $(grep -oE 'target *= *"L[^;"]+;[A-Za-z0-9_<>$]+' "$f" | sed -E 's/target *= *"L//'); do
    owner="${t%%;*}"; name="${t#*;}"; owner="${owner//\//.}"
    checked=$((checked+1))
    o2=$("$JAVAP" -p -cp "$CP" "$owner" 2>&1)
    has_member "$o2" "$name" || { echo "MISS at-target $owner#$name  ($f)"; miss=$((miss+1)); }
  done
done < <(find "$MIXDIR" -name "*.java" -print0 2>/dev/null)
echo "audit: checked=$checked missing=$miss"
[ "$miss" -eq 0 ]
