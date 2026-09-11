#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"
mkdir -p bin
javac -encoding UTF-8 --release 17 -d bin src/App.java
