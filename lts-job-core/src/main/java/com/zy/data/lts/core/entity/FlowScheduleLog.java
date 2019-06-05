package com.zy.data.lts.core.entity;

import java.util.Date;


/**
 * 工作流调度日志，记录工作流的调度流程
 */
public class FlowScheduleLog {

    private int id;

    private int flowTaskId;

    private String content;

    private Date createTime;


    public FlowScheduleLog() {}

    public FlowScheduleLog(int flowTaskId, String content) {
        this.flowTaskId = flowTaskId;
        this.content = content;
        this.createTime = new Date();
    }

    public int getFlowTaskId() {
        return flowTaskId;
    }

    public void setFlowTaskId(int flowTaskId) {
        this.flowTaskId = flowTaskId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
