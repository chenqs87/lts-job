package com.zy.data.lts.core.model;

import com.zy.data.lts.core.RoleEnum;

public class FlowQueryRequest extends PagerRequest {
    private String username;
    private RoleEnum role;
    private int permit;

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

    public RoleEnum getRole() {
        return role;
    }

    public void setRole(RoleEnum role) {
        this.role = role;
    }
}
