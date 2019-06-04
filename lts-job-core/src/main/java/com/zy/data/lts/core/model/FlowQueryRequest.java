package com.zy.data.lts.core.model;

public class FlowQueryRequest extends PagerRequest {
    private String username;
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
}
