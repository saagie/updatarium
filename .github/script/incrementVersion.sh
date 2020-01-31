#!/usr/bin/env bash

LOGS=$(git log --oneline "$(git describe --tags --abbrev=0 @^)..@")
BREAKING_CHANGE=false
ONLY_DOC_OR_BUILD=true

for log in $LOGS; do
  if [[ "$log" == :*: ]]; then
    echo "> [$log]"
    if [ "$log" == ":boom:" ]; then
      BREAKING_CHANGE=true
    fi
    if [ "$log" != ":pencil:" ] && [ "$log" != ":green_heart:" ] && [ "$log" != ":construction_worker:" ] && [ "$log" != ":busts_in_silhouette:" ] && [ "$log" != ":bookmark:" ]; then
      ONLY_DOC_OR_BUILD=false
    fi
  fi
done

if [ "$BREAKING_CHANGE" == true ]; then
  echo "BREAKING_CHANGE"
  ./gradlew incrementMajor
elif [ "$ONLY_DOC_OR_BUILD" == true ]; then
  echo "ONLY DOC OR BUILD"
  ./gradlew incrementPatch
else
  echo "NO BREAKING_CHANGE AND NOT ONLY DOC OR BUILD"
  ./gradlew incrementMinor
fi
