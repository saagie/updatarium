#!/usr/bin/env bash
docker run --name=test -v ${PWD}:/updatarium-src saagie/updatarium:latest /updatarium-src/samples/basic-multiplechangelog-withtag/src/main/resources/changelogs
docker run --name=test-tag -v ${PWD}:/updatarium-src saagie/updatarium:latest --tags=before /updatarium-src/samples/basic-multiplechangelog-withtag/src/main/resources/changelogs
docker run --name=test-tags -v ${PWD}:/updatarium-src saagie/updatarium:latest --tags=before --tags=after /updatarium-src/samples/basic-multiplechangelog-withtag/src/main/resources/changelogs
if [[ $(docker logs test | grep Hello | wc -l) == 20 ]]; then
  echo "OK simple test"
  if [[ $(docker logs test-tag | grep Hello | wc -l) == 10 ]]; then
    echo "OK test with tag"
    if [[ $(docker logs test-tags | grep Hello | wc -l) == 15 ]]; then
      echo "OK test with tags"
      exit 0
    else
      echo "KO test with tags"
      exit 1
    fi
  else
    echo "KO test with tag"
    exit 1
  fi

else
  echo "KO simple test"
  exit 1
fi
