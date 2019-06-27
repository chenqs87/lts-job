package com.zy.data.lts.core.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;

/**
 * 执行的Job, task 属于一个flow, 一个flow 最多不能超过32个子任务
 *
 * @author chenqingsong
 * @date 2019/3/28 15:49
 */
public class Task {


    /**
     * 关联的flow task id, 联合主键
     */
    private int flowTaskId;

    /**
     * 联合主键， 取值范围0~31
     */
    private int taskId;

    /**
     * 关联的jobId
     */
    private int jobId;

    /**
     * 关联的 flow id, 权限由 flow 的权限决定
     */
    private int flowId;

    /**
     * 作业状态
     */
    private volatile int taskStatus;

    /**
     * 前置任务id ，二进制表示，例如前置任务是0，1，则 preTask的二进制表示是11，十进制表示是3
     */
    private int preTask;

    /**
     * 后置任务，与preTask类似，当前任务执行完毕之后，要触发的后置任务
     */
    private int postTask;

    @JsonIgnore
    private Date beginTime;

    @JsonIgnore
    private Date endTime;

    private int shardStatus;

    private String handler = "";

    private String host = "";


    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
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

    public int getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(int taskStatus) {
        this.taskStatus = taskStatus;
    }

    public int getPreTask() {
        return preTask;
    }

    public void setPreTask(int preTask) {
        this.preTask = preTask;
    }

    public void setUpPreTask(int preTask) {
        this.preTask |= 1 << preTask;
    }

    public synchronized void completePreTask(int preTask) {
        this.preTask ^= 1 << preTask;
    }

    public int getPostTask() {
        return postTask;
    }

    public void setPostTask(int postTask) {
        this.postTask = postTask;
    }

    public void setUpPostTask(int postTask) {
        this.postTask |= 1 << postTask;
    }

    public int getFlowId() {
        return flowId;
    }

    public void setFlowId(int flowId) {
        this.flowId = flowId;
    }

    public Date getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
        // 任务未完成时，不生效
        this.endTime = beginTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public int getShardStatus() {
        return shardStatus;
    }

    public void setShardStatus(int shardStatus) {
        this.shardStatus = shardStatus;
    }

    public void completeShard(int shardStatus) {
        this.shardStatus ^= 1 << shardStatus;
    }

    public String getHandler() {
        return handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
