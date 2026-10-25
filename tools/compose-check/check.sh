#!/bin/sh
# Usage: tools/compose-check/check.sh [work-dir]
# Copies core/ and app/ sources (minus files that start with "// ANDROID-ONLY") plus shims into a scratch Gradle
# project and compiles it. Exit code 0 = the Compose code type-checks. Needs Maven Central only.
set -e
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
WORK="${1:-${TMPDIR:-/tmp}/topgun-compose-check}"
SRC="$WORK/src/main/kotlin"
rm -rf "$SRC"; mkdir -p "$SRC"
cp "$ROOT/tools/compose-check/settings.gradle.kts" "$ROOT/tools/compose-check/build.gradle.kts" "$WORK/"

cp -r "$ROOT/core/src/main/kotlin/." "$SRC/"
(cd "$ROOT/app/src/main/java" && find . -name '*.kt' | while read -r f; do
  if [ "$(head -n 1 "$f")" != "// ANDROID-ONLY" ]; then mkdir -p "$SRC/$(dirname "$f")"; cp "$f" "$SRC/$f"; fi
done)
cp -r "$ROOT/tools/compose-check/shims/." "$SRC/"

# R.kt: resource ids as zero constants, generated from file names so references to R.drawable.x / R.font.x resolve.
python3 - "$ROOT/app/src/main/res" "$SRC/R.kt" <<'PY'
import os, sys, re
res, out = sys.argv[1], sys.argv[2]
kinds = {}
for d in sorted(os.listdir(res)):
    kind = d.split('-')[0]
    p = os.path.join(res, d)
    if not os.path.isdir(p): continue
    for f in os.listdir(p):
        name = f.split('.')[0]
        if kind == 'values':
            if f.startswith('strings') and f.endswith('.xml'):
                for m in re.finditer(r'<(?:string|plurals|string-array) name="([^"]+)"', open(os.path.join(p, f)).read()):
                    kinds.setdefault('string', set()).add(m.group(1))
            continue
        kinds.setdefault(kind, set()).add(name)
with open(out, 'w') as o:
    o.write('package app.topgun.android\n\nobject R {\n')
    for k, names in sorted(kinds.items()):
        o.write('    object %s {\n' % k)
        for n in sorted(names):
            o.write('        const val %s = 0\n' % re.sub(r'[^A-Za-z0-9_]', '_', n))
        o.write('    }\n')
    o.write('}\n')
PY

cd "$WORK"
for attempt in 1 2 3 4 5; do
  if gradle compileKotlin --max-workers=1 >"$WORK/err.txt" 2>&1; then echo "compose-check: OK"; exit 0; fi
  if grep -q "429\|Too Many Requests" "$WORK/err.txt"; then echo "rate limited, retrying..."; sleep $((attempt * 30)); else break; fi
done
grep -E "^e: " "$WORK/err.txt" | sed "s|file://$WORK/src/main/kotlin/||" | head -80
[ -n "$(grep -E "^e: " "$WORK/err.txt")" ] || tail -30 "$WORK/err.txt"
exit 1
