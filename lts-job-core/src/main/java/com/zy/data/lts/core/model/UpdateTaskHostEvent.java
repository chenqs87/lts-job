package com.zy.data.lts.core.model;

/**
 * @author chenqingsong
 * @date 2019/4/30 15:49
 */
public class UpdateTaskHostEvent {
    private int flowTaskId;
    private int taskId;
    private String host;

    public UpdateTaskHostEvent(int flowTaskId, int taskId, String host) {
        this.flowTaskId = flowTaskId;
        this.taskId = taskId;
        this.host = host;
    }

    public int getFlowTaskId() {
        return flowTaskId;
    }

    public void setFlowTaskId(int flowTaskId) {
        this.flowTaskId = flowTaskId;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
