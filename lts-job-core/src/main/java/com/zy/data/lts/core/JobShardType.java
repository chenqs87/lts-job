package com.zy.data.lts.core;

/**
 * @author chenqingsong
 * @date 2019/4/8 14:41
 */
public enum JobShardType {
    NONE(0),
    HDFS_SHARD(1);

    int code;

    JobShardType(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
