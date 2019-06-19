package com.zy.data.lts.core.model;

public class ExecLogEvent {

    private int flowTaskId;
    private String msg;

    public ExecLogEvent(int flowTaskId, String msg) {
        this.flowTaskId = flowTaskId;
        this.msg = msg;
    }

    public int getFlowTaskId() {
        return flowTaskId;
    }

    public void setFlowTaskId(int flowTaskId) {
        this.flowTaskId = flowTaskId;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
