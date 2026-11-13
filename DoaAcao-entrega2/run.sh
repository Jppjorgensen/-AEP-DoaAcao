#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"
java -cp 'bin:lib/*' br.com.doaacao.App "$@"
