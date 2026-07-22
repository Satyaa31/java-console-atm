#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

bash "$ROOT/scripts/prepare-javafx.sh"
mvn -q -DskipTests compile

MODULE_PATH="$ROOT/lib/javafx"
exec java \
  --module-path "$MODULE_PATH" \
  --add-modules javafx.controls,javafx.graphics,javafx.base \
  -cp "$ROOT/target/classes" \
  com.atm.gui.ATMGuiApp
