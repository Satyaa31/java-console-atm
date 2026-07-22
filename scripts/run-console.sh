#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

bash "$ROOT/scripts/prepare-javafx.sh"
mvn -q -DskipTests compile
exec java -cp "$ROOT/target/classes" com.atm.main.ATMApplication
