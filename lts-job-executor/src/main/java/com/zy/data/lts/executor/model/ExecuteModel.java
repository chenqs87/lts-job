package com.zy.data.lts.executor.model;

/**
 * @author chenqingsong
 * @date 2019/4/9 17:40
 */
public class ExecuteModel {
    private int flowTaskId;
    private int taskId;
    private int shard;

    public ExecuteModel() { }

    public ExecuteModel(int flowTaskId, int taskId, int shard) {
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
