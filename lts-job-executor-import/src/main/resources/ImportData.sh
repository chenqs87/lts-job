#!/usr/bin/env bash
#set -x
params=$1
oldConfig="/user/pub/old_jar_zip/path"
newConfig="/user/pub/old_jar_zip/path"
hadoop_config="${HaoopConfigDir}"

echo "[ImportData][INFO] params:${params}"

inputFile="`echo ${params} | grep -Po 'input[":]+\K[^"]+'`"

type="`echo ${params} | grep -Po 'type[":]+\K[^"]+'`"

if [ -z "${inputFile}" ] || [ "x${inputFile}" = "x" ]; then
    echo "[ImportData][INFO] Param [input] is needed!"
    exit -1
fi

java_zip_path=""
if [ "x${type}" = "x" ]; then
    echo "[ImportData][INFO] Param [type] is needed!"
    exit -1
elif [ "x${type}" = "xold" ]; then
    java_zip_path="${oldConfig}"
    echo "[ImportData][INFO] Will use config : ${oldConfig}"
else
    java_zip_path="${newConfig}"
    echo "[ImportData][INFO] Will use config : ${newConfig}"
fi

exec_dir=$(cd `dirname $0`; pwd)

echo "[ImportData][INFO] begin to get file [${java_zip_path}] from hdfs !"

hadoop --config ${hadoop_config} fs -get ${java_zip_path} ${exec_dir}

cd ${exec_dir}; unzip ./*.zip


echo "[ImportData][INFO] success to get file !"

echo "=============================== begin import data ==============================="

format_param=$(echo "${params}" | awk 'BEGIN{ORS=" "}{print $0}'| sed s/[[:space:]]//g)

jar_file=""
main_class=""

if [ "x${type}" = "xold" ]; then
    jar_file="./rec-datatools.jar"
    main_class="com.zy.rec.data.lts.LtsJobImportMain"
else
    jar_file="./azkaban-job.jar"
    main_class="com.zy.data.tool.lts.LtsJobImportMain"
fi

hadoop_config="./hadoop-config"

if [ ! -f "${jar_file}" ];then
    echo "[ImportData][INFO] File [azkaban-job.jar] is not exist!"
    exit -1
fi

if [ ! -d "${hadoop_config}" ];then
    echo "[ImportData][INFO] Dir [hadoop-config] is not exist!"
    exit -1
fi

hadoop --config ${hadoop_config} jar ${jar_file} ${main_class} ${format_param}

exit $?

echo "=============================== end import data ================================"