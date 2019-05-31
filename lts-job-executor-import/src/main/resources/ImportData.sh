#!/usr/bin/env bash
#set -x
params=$1
oldConfig="${OldVersionImportConfig}"
newConfig="${NewVersionImportConfig}"
hadoop_config="${HaoopConfigDir}"

echo "[ImportData][INFO] params:${params}"

inputFile="`echo ${params} | grep -Po 'input[":]+\K[^"]+'`"

type="`echo ${params} | grep -Po 'type[":]+\K[^"]+'`"

if [ -z "${inputFile}" ] || [ "x${inputFile}" = "x" ]; then
    echo "[ImportData][INFO] Param [input] is needed!"
    exit -1
fi

import_config=""
if [ "x${type}" = "x" ]; then
    echo "[ImportData][INFO] Param [type] is needed!"
    exit -1
elif [ "x${type}" = "xold" ]; then
    import_config="${oldConfig}"
    echo "[ImportData][INFO] Will use config : ${oldConfig}"
else
    import_config="${newConfig}"
    echo "[ImportData][INFO] Will use config : ${newConfig}"
fi


exec_dir=$(cd `dirname $0`; pwd)

config_file=$(echo "`hadoop --config ${hadoop_config} fs -cat ${import_config} | head -1`")

if [ "x${config_file}" = "x" ]; then
    echo "[ImportData][INFO] ImportConfig file [${import_config}] is empty!"
    exit -1
fi

echo "[ImportData][INFO] begin to get file [${config_file}] from hdfs !"

hadoop --config ${hadoop_config} fs -get ${config_file} ${exec_dir}

cd ${exec_dir}; unzip ./*.zip



echo "[ImportData][INFO] success to get file !"

echo "=============================== begin import data ==============================="

format_param=$(echo "${params}" | awk 'BEGIN{ORS=" "}{print $0}'| sed s/[[:space:]]//g)


jar_file="./azkaban-job.jar"
main_class="com.zy.data.tool.lts.LtsJobImportMain"
hadoop_config="./hadoop-config"
hadoop --config ${hadoop_config} jar ${jar_file} ${main_class} ${format_param}

#sh ${exec_dir}/import-data.sh 0 "${format_param}"

echo "=============================== end import data ================================"