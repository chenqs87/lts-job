#!/usr/bin/env bash

set -x
execFile=$1
params=$2

echo "[CheckFileContent][INFO] script:${execFile}"
echo "[CheckFileContent][INFO] script content: "
echo "==========================================="
cat ${execFile}
echo "\n"
echo "==========================================="

echo "[CheckFileContent][INFO] input params: ${params}"

inputFile="`echo ${params} | grep -Po 'input[":]+\K[^"]+'`"
type="`echo ${params} | grep -Po 'type[":]+\K[^"]+'`"

echo "[CheckFileContent][INFO] HDFS Input File: ${inputFile}"

echo "[CheckFileContent][INFO] HDFS Input Type: ${type}"

if [ -z "${inputFile}" ] || [ "x${inputFile}" = "x" ]; then
    echo "[CheckFileContent][INFO] Param [input] is needed!"
    exit -1
fi

if [ "x${type}" = "x" ]; then
    echo "[CheckFileContent][INFO] Param [type] is needed!"
    exit -1
elif [ "x${type}" = "xold" ]; then
    echo "[CheckFileContent][INFO] Will use config : ${OldVersionImportConfig}"
else
    echo "[CheckFileContent][INFO] Will use config : ${NewVersionImportConfig}"
fi

echo "[CheckFileContent][INFO] NewVersionImportConfig: ${NewVersionImportConfig}"

echo "[CheckFileContent][INFO] OldVersionImportConfig: ${OldVersionImportConfig}"

echo "[CheckFileContent][INFO] OldVersionImportConfig: ${OldVersionImportConfig}"


exec_dir=$(cd `dirname $0`; pwd)

# hdfs dfs get ${NewVersionImportConfig} ${exec_dir}

config_file=$(echo "`cat ${NewVersionImportConfig}`")

echo "[CheckFileContent][INFO] ConfigFilePath: ${config_file}"

echo "------------------------- check input file ----------------------------"
cat ${inputFile} | python ${execFile}