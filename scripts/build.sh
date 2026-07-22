#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

bash "$ROOT/scripts/prepare-javafx.sh"
mvn -q -DskipTests compile
echo "Build OK → target/classes"
