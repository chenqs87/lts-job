package com.zy.data.lts.naming.handler;

import org.springframework.context.ApplicationEvent;

public class LtsHandlerChangeEvent extends ApplicationEvent {
    private String handlerName;
    private HandlerEventType handlerEventType;
    private String host;

    public LtsHandlerChangeEvent(String handlerName, HandlerEventType handlerEventType, String host) {
        super(handlerName);
        this.handlerName = handlerName;
        this.handlerEventType = handlerEventType;
        this.host = host;
    }

    public String getHandlerName() {
        return handlerName;
    }

    public void setHandlerName(String handlerName) {
        this.handlerName = handlerName;
    }

    public HandlerEventType getHandlerEventType() {
        return handlerEventType;
    }

    public void setHandlerEventType(HandlerEventType handlerEventType) {
        this.handlerEventType = handlerEventType;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
