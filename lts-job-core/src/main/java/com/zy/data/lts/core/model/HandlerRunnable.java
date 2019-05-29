package com.zy.data.lts.core.model;

public abstract class HandlerRunnable implements Runnable {

    private String handlerName;

     HandlerRunnable (String handlerName) {
        this.handlerName = handlerName;
    }
}
