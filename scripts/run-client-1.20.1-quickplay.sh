#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RUN_DIR="$ROOT_DIR/.runs/client"
SAVES_DIR="$RUN_DIR/saves"

TARGET_WORLD="${LOCOMOTION_QUICKPLAY_WORLD:-QuickTest}"

mkdir -p "$SAVES_DIR"

if [ ! -d "$SAVES_DIR/$TARGET_WORLD" ]; then
  SOURCE_WORLD_PATH="${LOCOMOTION_QUICKPLAY_SOURCE:-}"
  if [ -z "$SOURCE_WORLD_PATH" ]; then
    DEFAULT_PRISM_SAVES="/home/grob_ik/.var/app/org.prismlauncher.PrismLauncher/data/PrismLauncher/instances/Yomacraft- Mods 2/minecraft/saves"
    if [ -d "$DEFAULT_PRISM_SAVES" ]; then
      SOURCE_WORLD_PATH="$(find "$DEFAULT_PRISM_SAVES" -mindepth 1 -maxdepth 1 -type d | head -n 1 || true)"
    fi
  fi

  if [ -n "$SOURCE_WORLD_PATH" ] && [ -d "$SOURCE_WORLD_PATH" ]; then
    echo "Copying world from: $SOURCE_WORLD_PATH"
    cp -a "$SOURCE_WORLD_PATH" "$SAVES_DIR/$TARGET_WORLD"
  else
    echo "No source world found to copy. Ensure $SAVES_DIR/$TARGET_WORLD exists before running quick play."
  fi
fi

export LOCOMOTION_QUICKPLAY_WORLD="$TARGET_WORLD"

cd "$ROOT_DIR"
./gradlew chiseledBuildFabric :fabric:1.20.1:runClient --offline
