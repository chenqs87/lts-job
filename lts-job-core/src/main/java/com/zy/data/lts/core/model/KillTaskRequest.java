package com.zy.data.lts.core.model;

import com.zy.data.lts.core.entity.Task;

/**
 * @author chenqingsong
 * @date 2019/5/8 11:32
 */
public class KillTaskRequest {

    private Task task;
    private int shard;

    public KillTaskRequest() {
    }

    public KillTaskRequest(Task task, int shard) {
        this.task = task;
        this.shard = shard;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public int getShard() {
        return shard;
    }

    public void setShard(int shard) {
        this.shard = shard;
    }

    public String toString() {
        return "{host: " + task.getHost() + ", flowTaskId: " + task.getFlowTaskId()
                + ", taskId: " + task.getTaskId() + ", shard:" + shard + " }";
    }
}
