#!/usr/bin/env bash
# Monta o projeto Android a partir de ../entregas.html (rodado pelo GitHub Actions).
set -euo pipefail
cd "$(dirname "$0")"

rm -rf www android
mkdir -p www
cp ../entregas.html www/index.html
cp node_modules/@supabase/supabase-js/dist/umd/supabase.js www/supabase.js

# usa a biblioteca do Supabase de dentro do APK (funciona sem internet) em vez da do unpkg
python3 - <<'PY'
p = 'www/index.html'
s = open(p, encoding='utf-8').read()
tag = '<script src="https://unpkg.com/@supabase/supabase-js@2"></script>'
assert tag in s, 'tag do Supabase não encontrada em entregas.html'
open(p, 'w', encoding='utf-8').write(s.replace(tag, '<script src="supabase.js"></script>'))
PY

npx cap add android

PKG=android/app/src/main/java/br/com/vanessadoces/entregas
cp native/MainActivity.java "$PKG/MainActivity.java"
cp native/NativoPlugin.java "$PKG/NativoPlugin.java"

# permite listar os apps instalados (Android 11+) — no Android 9 não é necessário, mas não atrapalha
python3 - <<'PY'
p = 'android/app/src/main/AndroidManifest.xml'
s = open(p, encoding='utf-8').read()
q = """    <queries>
        <intent>
            <action android:name="android.intent.action.MAIN" />
            <category android:name="android.intent.category.LAUNCHER" />
        </intent>
    </queries>
"""
assert '</manifest>' in s
open(p, 'w', encoding='utf-8').write(s.replace('</manifest>', q + '</manifest>'))
PY

npx cap sync android
echo "Projeto Android pronto."
