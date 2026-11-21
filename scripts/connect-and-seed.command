#!/bin/bash
set -euo pipefail
cd "$(dirname "$0")/.."
export FIRESTORE_EMULATOR_HOST=127.0.0.1:8280
export FIREBASE_AUTH_EMULATOR_HOST=127.0.0.1:9199
npm --prefix backend run seed
adb_path="${ANDROID_HOME:-$HOME/Library/Android/sdk}/platform-tools/adb"
"$adb_path" reverse tcp:9099 tcp:9199
"$adb_path" reverse tcp:8080 tcp:8280
echo 'Keep the backend terminal open. Sign in using one of the local accounts printed above.'
