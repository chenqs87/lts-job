package com.zy.data.lts.naming.master;

public class MasterUnConnectedEvent {
    private String host;

    public MasterUnConnectedEvent(String host) {
        this.host = host;
    }

    public String getHost() {
        return host;
    }
}
