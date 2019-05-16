package com.zy.data.lts.core.entity;

import java.util.Date;

/**
 * @author chenqingsong
 * @date 2019/5/15 17:06
 */
public class Group {
    private String groupName;
    private Date createTime;
    private String remark;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
