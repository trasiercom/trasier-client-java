#!/usr/bin/env bash

set -e
set -o pipefail

GITHUB_USER=$1
GITHUB_PWD=$2

if [[ $GITHUB_USER == '' ]]; then
    echo "Github username must be set"
    exit -1;
fi


if [[ $GITHUB_PWD == '' ]]; then
    echo "Github password must be set"
    exit -1;
fi


echo "[TRASIER] Testing the build... "
mvn clean install

echo "[TRASIER] Starting release... "
mvn jgitflow:release-start jgitflow:release-finish -B -Dmaven.test.skip=true -s .notes/settings-local.xml -DGITHUB_USER=$GITHUB_USER -DGITHUB_PASSWORD=$GITHUB_PWD -Prelease || echo "Release failed. Check logs for details."

echo "[TRASIER] Merging master back into develop... "
git merge origin/master --strategy-option ours || echo "Something went wrong while merging master into develop."

echo "[TRASIER] Release finished."