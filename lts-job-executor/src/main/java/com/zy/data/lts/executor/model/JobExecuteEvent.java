package com.zy.data.lts.executor.model;

import com.zy.data.lts.core.entity.Task;

import java.nio.file.Path;
import java.util.Calendar;

/**
 * @author chenqingsong
 * @date 2019/4/9 17:12
 */
public class JobExecuteEvent {
    private int flowTaskId;
    private int taskId;
    private int flowId;
    private int jobId;
    private int shard = 0;
    private String jobType;

    /**
     * 脚本所在目录
     */
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

    public JobExecuteEvent(Task task, Path output, String params, String jobType) {
        this.flowTaskId = task.getFlowTaskId();
        this.taskId = task.getTaskId();
        this.flowId = task.getFlowId();
        this.jobId = task.getJobId();
        this.params = params;
        this.jobType = jobType;
        this.output = output;
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

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public int getFlowId() {
        return flowId;
    }

    public void setFlowId(int flowId) {
        this.flowId = flowId;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }
}
