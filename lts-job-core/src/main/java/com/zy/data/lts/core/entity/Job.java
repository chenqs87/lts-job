package com.zy.data.lts.core.entity;

import java.util.Date;

/**
 * 作业配置
 * @author chenqingsong
 * @date 2019/3/28 14:15
 */
public class Job {
    private Integer id;

    private String name;

    /**
     * executor 所对应的handler，相当于handler的一个分组
     */
    private String handler;

    /**
     * 作业类型 shell python java 等等
     */
    private int jobType;

    private int shardType;

    private Date createTime;

    private int createUser;

    /**
     * Job的内容，如果是脚本则为脚本的内容
     */
    private String content;

    /**
     * 权限二进制表示
     */
    private int permit;

    /**
     * 用户自定义配置，分配任务，需要配置在config 中
     * json 格式
     * {
     *     shardCount: 5
     * }
     */
    private String config;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHandler() {
        return handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }

    public int getJobType() {
        return jobType;
    }

    public void setJobType(int jobType) {
        this.jobType = jobType;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public int getCreateUser() {
        return createUser;
    }

    public void setCreateUser(int createUser) {
        this.createUser = createUser;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getPermit() {
        return permit;
    }

    public int getShardType() {
        return shardType;
    }

    public void setShardType(int shardType) {
        this.shardType = shardType;
    }

    public void setPermit(int permit) {
        this.permit = permit;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }
}
