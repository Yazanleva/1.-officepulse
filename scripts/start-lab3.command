#!/bin/bash
set -euo pipefail
cd "$(dirname "$0")/.."
if [ -d '/Applications/Android Studio.app/Contents/jbr/Contents/Home' ]; then
  export JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home'
fi
if ! command -v npm >/dev/null; then
  echo 'Install Node.js 22 (includes npm), then run this file again.'
  exit 1
fi
npm ci --prefix functions
npm run build --prefix functions
npm ci --prefix backend
backend/node_modules/.bin/firebase emulators:start --config firebase.lab3.json --project demo-officepulse
