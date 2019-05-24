#!/usr/bin/env bash

execFile=$1
params=$2

echo "Path:::::${execFile}"
echo "Params:::::${params}"

cat /Users/chenqs/test/test.log | python ${execFile}