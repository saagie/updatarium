#!/usr/bin/env bash

LOGS=$(git log --oneline $(git describe --tags --abbrev=0 @^)..@)
BREAKING_CHANGE=false

for log in $LOGS
do
    echo "> [$log]"
    if [ "$log" == ":boom:" ]; then
      BREAKING_CHANGE=true;
    fi
done

if [ "$BREAKING_CHANGE" == true ]; then
  echo "BREAKING_CHANGE"
  ./gradlew incrementMajor
else
  echo "NO BREAKING_CHANGE"
  ./gradlew incrementMinor
fi