package com.zy.data.lts.core.entity;

import java.util.Date;

/**
 * 对应Flow，执行的工作流
 * @author chenqingsong
 * @date 2019/3/28 15:49
 */
public class FlowTask {
    private int id;
    private int flowId;
    private int status;
    private Date beginTime;
    private Date endTime;
    private String params;
    private int createUser;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFlowId() {
        return flowId;
    }

    public void setFlowId(int flowId) {
        this.flowId = flowId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }


    public Date getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
        //任务未完成时，不生效
        this.endTime = beginTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public int getCreateUser() {
        return createUser;
    }

    public void setCreateUser(int createUser) {
        this.createUser = createUser;
    }
}
