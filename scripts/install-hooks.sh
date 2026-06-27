#!/bin/sh
git config core.hooksPath .githooks
chmod +x .githooks/pre-commit
echo "Hooks instalados. Pre-commit ativo em .githooks/"
