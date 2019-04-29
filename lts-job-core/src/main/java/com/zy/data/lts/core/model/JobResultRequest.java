package com.zy.data.lts.core.model;

/**
 * @author chenqingsong
 * @date 2019/4/10 16:01
 */
public class JobResultRequest {
    private int flowTaskId;
    private int taskId;
    private int shard;

    public JobResultRequest() {}

    public JobResultRequest(int flowTaskId, int taskId, int shard) {
        this.flowTaskId = flowTaskId;
        this.taskId = taskId;
        this.shard = shard;
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
