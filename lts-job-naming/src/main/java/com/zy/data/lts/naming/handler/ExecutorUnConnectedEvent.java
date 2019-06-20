package com.zy.data.lts.naming.handler;

import com.zy.data.lts.core.model.Executor;

public class ExecutorUnConnectedEvent {


    private Executor executor;

    public ExecutorUnConnectedEvent(Executor executor) {
        this.executor = executor;
    }

    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }
}
