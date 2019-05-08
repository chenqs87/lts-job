package com.zy.data.lts.executor.model;

/**
 * @author chenqingsong
 * @date 2019/5/7 20:54
 */
public class KillJobEvent {

    private int flowTaskId;
    private int taskId;
    private int shard;

    public KillJobEvent(int flowTaskId, int taskId, int shard) {
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
