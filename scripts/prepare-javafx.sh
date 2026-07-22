#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if ! command -v mvn >/dev/null 2>&1; then
  echo "Maven (mvn) is required. Install it, then retry."
  exit 1
fi

echo "Downloading JavaFX (Maven) if needed..."
mvn -q -DskipTests dependency:copy-dependencies@copy-javafx

if [[ ! -d "$ROOT/lib/javafx" ]] || ! ls "$ROOT/lib/javafx"/javafx-base*.jar >/dev/null 2>&1; then
  echo "JavaFX jars missing under lib/javafx"
  echo "Run: mvn -U dependency:copy-dependencies@copy-javafx"
  exit 1
fi

echo "JavaFX ready → lib/javafx"
