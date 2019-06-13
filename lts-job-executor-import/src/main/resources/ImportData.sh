#!/usr/bin/env bash
#set -x
params=$(echo "$1" | awk 'BEGIN{ORS=" "}{print $0}'| sed s/[[:space:]]//g)

#导入数据老的程序包，对应项目rec-datatools,生产环境
oldConfig="/user/pub/cqs/import-data/new/azkaban-job.zip"
#老程序包灰度环境
oldGreyConfig="/user/pub/cqs/import-data/new/azkaban-job.zip"

#导入数据新程序包，对应项目data-x-zy,生产环境
newConfig="/user/pub/cqs/import-data/new/azkaban-job.zip"

#导入数据新程序包灰度环境
newGreyConfig="/user/pub/cqs/import-data/new/azkaban-job.zip"

hadoop_config="${HaoopConfigDir}"

echo "[ImportData][INFO] params:${params}"

inputFile="`echo ${params} | grep -Po 'input[":]+\K[^"]+'`"

type="`echo ${params} | grep -Po 'type[":]+\K[^"]+'`"

execEnv="`echo ${params} | grep -Po 'env[":]+\K[^"]+'`"

if [ -z "${inputFile}" ] || [ "x${inputFile}" = "x" ]; then
    echo "[ImportData][INFO] Param [input] is needed!"
    exit -1
fi

java_zip_path=""
if [ "x${type}" = "x" ]; then
    echo "[ImportData][INFO] Param [type] is needed!"
    exit -1
elif [ "x${type}" = "xold" ]; then
    if [ "x${execEnv}" = "xprod" ]; then
        java_zip_path="${oldConfig}"
    else
        java_zip_path="${oldGreyConfig}"
    fi

else
    if [ "x${execEnv}" = "xprod" ]; then
        java_zip_path="${newConfig}"
    else
        java_zip_path="${newGreyConfig}"
    fi
fi

echo "[ImportData][INFO] Will use config : ${java_zip_path}"

exec_dir=$(cd `dirname $0`; pwd)

echo "[ImportData][INFO] begin to get file [${java_zip_path}] from hdfs !"

hadoop --config ${hadoop_config} fs -get ${java_zip_path} ${exec_dir}

if [ $? -ne 0 ]; then
    echo "[ImportData][INFO] Fail to get file !"
    exit -1
fi

cd ${exec_dir}; unzip ./*.zip

if [ $? -ne 0 ]; then
    echo "[ImportData][INFO] Fail to unzip the zip !"
    exit -1
fi

echo "[ImportData][INFO] Success to get file !"

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