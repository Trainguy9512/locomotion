#!/usr/bin/env bash
set -euo pipefail

# Build and verify in a single Gradle invocation so Stonecutter stays active.
./gradlew chiseledBuildFabric :fabric:1.20.1:verifyFabricJar

echo "OK: Fabric 1.20.1 jar verified."
