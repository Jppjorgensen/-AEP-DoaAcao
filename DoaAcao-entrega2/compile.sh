#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"
rm -rf bin
mkdir -p bin
javac -encoding UTF-8 --release 17 -cp "lib/*" -sourcepath src -d bin src/br/com/doaacao/App.java
echo "Tudo isso foi compilado com sucesso. Agora rode: bash run.sh"
