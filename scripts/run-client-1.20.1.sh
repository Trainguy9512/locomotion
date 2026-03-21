#!/usr/bin/env bash
set -euo pipefail

# Run a chiseled task in the same invocation so Stonecutter preprocesses version-guarded sources.
# Then launch the Fabric dev client for 1.20.1.
GRADLE_CMD=("./gradlew" "chiseledBuildFabric" ":fabric:1.20.1:runClient")

if command -v xvfb-run >/dev/null 2>&1; then
  xvfb-run -a "${GRADLE_CMD[@]}"
else
  "${GRADLE_CMD[@]}"
fi
