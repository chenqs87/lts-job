package com.zy.data.lts.core.model;

/**
 * @author chenqingsong
 * @date 2019/5/8 11:32
 */
public class KillTaskRequest {

    private String host;
    private int flowTaskId;
    private int taskId;
    private int shard;

    public KillTaskRequest() {
    }

    public KillTaskRequest(String host, int flowTaskId, int taskId, int shard) {
        this.host = host;
        this.flowTaskId = flowTaskId;
        this.taskId = taskId;
        this.shard = shard;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
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

    public int getShard() {
        return shard;
    }

    public void setShard(int shard) {
        this.shard = shard;
    }

    public String toString() {
        return "{host: " + host + ", flowTaskId: " + flowTaskId + ", taskId: " + taskId + ", shard:" + shard + " }";
    }
}
