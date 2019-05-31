#!/usr/bin/env bash

set -x
execFile=$1
params=$2

hadoop_config="${HaoopConfigDir}"


echo "[CheckFileContent][INFO] script:${execFile}"
echo "[CheckFileContent][INFO] script content: "
echo "==========================================="
cat ${execFile}
echo "\n"
echo "==========================================="

echo "[CheckFileContent][INFO] input params: ${params}"

inputFile="`echo ${params} | grep -Po 'input[":]+\K[^"]+'`"

echo "------------------------- check input file ----------------------------"

hadoop --config ${hadoop_config} fs -text ${inputFile} | head -1000 | python ${execFile}

echo "------------------------- success ----------------------------"
