package com.zy.data.lts.core;

/**
 * @author chenqingsong
 * @date 2019/5/7 11:53
 */
public enum TriggerMode {

    Click(0, "手动触发"),
    PreTask(1, "前置任务触发"),
    Cron(2, "定时任务触发");

    private int code;
    private String msg;

    TriggerMode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
