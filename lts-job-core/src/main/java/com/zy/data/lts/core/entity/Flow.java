package com.zy.data.lts.core.entity;

import java.util.Date;

/**
 * 工作流配置
 *
 * @author chenqingsong
 * @date 2019/3/28 15:47
 */
public class Flow {
    private int id;

    private String name;
    /**
     * 任务关系 DAG 关系图
     * 示例：
     * 0：1
     * 1：2
     * 1：3
     * 2：4
     * 3：3
     */
    private String flowConfig;

    private String cron;

    /**
     * 0 未调度  1 cron 调度
     */
    private int flowStatus;

    /**
     * flow 创建时间
     */
    private Date createTime;

    /**
     * 定时任务启动时间
     */
    private Date startTime;


    /**
     * 创建工作流的用户
     */
    private int createUser;

    /**
     * 二进制表示，
     * 例如1， 1<<1 , 1<<2 等等
     */
    private int permit;

    /**
     * 定时任务启动时需要的默认参数配置
     */
    private String params;

    private int isSchedule;

    private String flowEditorInfo;

    /**
     * 后置工作流（子工作流），当前工作流执行成功后触发
     */
    private String postFlow;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFlowConfig() {
        return flowConfig;
    }

    public void setFlowConfig(String flowConfig) {
        this.flowConfig = flowConfig;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public int getFlowStatus() {
        return flowStatus;
    }

    public void setFlowStatus(int flowStatus) {
        this.flowStatus = flowStatus;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public int getCreateUser() {
        return createUser;
    }

    public void setCreateUser(int createUser) {
        this.createUser = createUser;
    }

    public int getPermit() {
        return permit;
    }

    public void setPermit(int permit) {
        this.permit = permit;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }


    public int getIsSchedule() {
        return isSchedule;
    }

    public void setIsSchedule(int isSchedule) {
        this.isSchedule = isSchedule;
    }

    public String getFlowEditorInfo() {
        return flowEditorInfo;
    }

    public void setFlowEditorInfo(String flowEditorInfo) {
        this.flowEditorInfo = flowEditorInfo;
    }

    public String getPostFlow() {
        return postFlow;
    }

    public void setPostFlow(String postFlow) {
        this.postFlow = postFlow;
    }
}
