package com.zy.data.lts.core.model;

/**
 * @author chenqingsong
 * @date 2019/5/7 09:42
 */
public class JobQueryRequest extends PagerRequest {

    private String name;
    private String group;
    private String userGroup;
    private String username;
    private int permit;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getUserGroup() {
        return userGroup;
    }

    public void setUserGroup(String userGroup) {
        this.userGroup = userGroup;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getPermit() {
        return permit;
    }

    public void setPermit(int permit) {
        this.permit = permit;
    }
}
