package com.zy.data.lts.executor.model;

import java.nio.file.Path;

/**
 * @author chenqingsong
 * @date 2019/4/9 17:12
 */
public class JobExecuteEvent {
    private int flowTaskId;
    private int taskId;
    private int shard;
    private Path output;
    private String params;

    public JobExecuteEvent() {
    }

    public JobExecuteEvent(int flowTaskId, int taskId, int shard, Path output, String params) {
        this.flowTaskId = flowTaskId;
        this.taskId = taskId;
        this.shard = shard;
        this.output = output;
        this.params = params;
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

    public Path getOutput() {
        return output;
    }

    public void setOutput(Path output) {
        this.output = output;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }
}
