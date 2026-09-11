#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"
java -cp 'bin:lib/*' App "$@"
