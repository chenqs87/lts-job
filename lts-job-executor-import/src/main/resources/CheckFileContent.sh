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

if [ "x${inputFile}" = "x" ]; then
    echo "[ImportData][INFO] ImportConfig file [${inputFile}] is empty!"
    exit -1
fi

stat=$(hadoop --config ${hadoop_config} fs -stat ${inputFile})

if [ "x${stat}" = "x" ]; then
    echo "[ImportData][INFO] ImportConfig file [${inputFile}] is empty!"
    exit -1
fi


echo "------------------------- check input file ----------------------------"

hadoop --config ${hadoop_config} fs -text ${inputFile} | head -1000 | python ${execFile}

echo "------------------------- success ----------------------------"
