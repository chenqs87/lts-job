package com.zy.data.lts.core.model;

/**
 * @author chenqingsong
 * @date 2019/4/10 16:42
 */
public class ExecuteRequest {

    private String handler;
    private int flowTaskId;
    private int taskId;
    private int shard;

    public ExecuteRequest() {}

    public ExecuteRequest(int flowTaskId, int taskId, int shard, String handler) {
        this.flowTaskId = flowTaskId;
        this.taskId = taskId;
        this.shard = shard;
        this.handler = handler;
    }

    public String getHandler() {
        return handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
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
}
