package com.zy.data.lts.naming;

import com.zy.data.lts.core.model.Executor;
import org.springframework.context.ApplicationEvent;

public class LtsHandlerChangeEvent extends ApplicationEvent {
    private String handlerName;
    private HandlerEventType handlerEventType;
    private Executor executor;

    public LtsHandlerChangeEvent(String handlerName, HandlerEventType handlerEventType, Executor executor) {
        super(handlerName);
        this.handlerName = handlerName;
        this.handlerEventType = handlerEventType;
        this.executor = executor;
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

    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }
}
