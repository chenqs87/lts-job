package com.zy.data.lts.core.entity;

import java.util.Date;

/**
 * 作业配置
 *
 * @author chenqingsong
 * @date 2019/3/28 14:15
 */
public class Job extends RepmPolicy {
    private Integer id;

    private String name;

    /**
     * executor 所对应的handler，相当于handler的一个分组
     */
    private String handler;

    /**
     * 作业类型 shell python java 等等
     */
    private String jobType;

    private int shardType;

    private Date createTime;

    private String createUser;

    /**
     * Job的内容，如果是脚本则为脚本的内容
     */
    private String content;

    /**
     * 用户自定义配置，分配任务，需要配置在config 中
     * json 格式
     * {
     * shardCount: 5
     * }
     */
    private String config;

    private String group = "default";


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

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


    public int getShardType() {
        return shardType;
    }

    public void setShardType(int shardType) {
        this.shardType = shardType;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}
