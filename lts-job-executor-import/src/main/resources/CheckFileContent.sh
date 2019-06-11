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

hadoop --config ${hadoop_config} fs -e ${inputFile}

if [ $? -ne 0 ]; then
    echo "[ImportData][INFO] ImportConfig file [${inputFile}] is not exist!"
    exit -1
fi

#判断是否是文件
hadoop --config ${hadoop_config} fs -f ${inputFile}

input="${inputFile}"

if [ $? -ne 0 ]; then
    hadoop --config ${hadoop_config} fs -d ${inputFile}
    if [ $? -eq 0 ]; then
        input="${inputFile}/*"
    else
        echo "[ImportData][INFO] ImportConfig file [${inputFile}] is not a file or a directory!"
        exit -1
    fi

fi

echo "------------------------- check input file ----------------------------"

hadoop --config ${hadoop_config} fs -text ${input} | head -1000 | python ${execFile}

exit $?

