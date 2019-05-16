package com.zy.data.lts.core.entity;

import java.util.Date;

/**
 * @author chenqingsong
 * @date 2019/5/14 14:11
 */
public class RepmPolicy {
    private String policyName;
    private String type;
    private int resource;
    private int permit;
    private Date createTime;

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getResource() {
        return resource;
    }

    public void setResource(int resource) {
        this.resource = resource;
    }

    public int getPermit() {
        return permit;
    }

    public void setPermit(int permit) {
        this.permit = permit;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
