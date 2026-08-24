#!/bin/sh
set -e

OLD_CONTRACT="$INPUT_OLD-CONTRACT"
NEW_CONTRACT="$INPUT_NEW-CONTRACT"
JSON_OUTPUT="$INPUT_JSON-OUTPUT"

if [ "$JSON_OUTPUT" = "true" ]; then
    java -jar /app/contract-drift-detector.jar "$OLD_CONTRACT" "$NEW_CONTRACT" --json
else
    java -jar /app/contract-drift-detector.jar "$OLD_CONTRACT" "$NEW_CONTRACT"
fi

exit $?
