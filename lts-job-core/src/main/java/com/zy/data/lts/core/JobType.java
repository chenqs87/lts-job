package com.zy.data.lts.core;

/**
 * @author chenqingsong
 * @date 2019/4/9 17:44
 */
public enum JobType {
    shell(1, ".sh"),
    python(2, ".py"),
    zip(3, ".zip"),

    /**
     * java executor 内部执行
     */
    java(4, ".java"),
    end(-1,"");


    int code;
    String suffix;
    JobType(int code, String suffix) {
        this.code = code;
        this.suffix = suffix;
    }

    public static JobType parse(int code) {
        switch (code) {
            case 1: return shell;
            case 2: return python;
            case 3: return zip;
            case -1: return end;
            default: throw new IllegalArgumentException("code is illegal!!!");
        }
    }

    public String suffix() {
        return suffix;
    }

    public int code() {
        return code;
    }
}
