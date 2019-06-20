package com.zy.data.lts.naming.master;

import org.springframework.context.ApplicationEvent;

public class LtsMasterChangeEvent extends ApplicationEvent {
    private MasterEventType eventType;

    public LtsMasterChangeEvent(String host, MasterEventType eventType) {
        super(host);
        this.eventType = eventType;
    }

    public String getHost() {
        return getSource().toString();
    }

    public MasterEventType getEventType() {
        return eventType;
    }
}
