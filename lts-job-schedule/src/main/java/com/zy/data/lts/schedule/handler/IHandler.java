package com.zy.data.lts.schedule.handler;

import com.zy.data.lts.core.api.IExecutorApi;
import com.zy.data.lts.core.model.Executor;

import java.io.Closeable;
import java.util.function.Consumer;

/**
 * @author chenqingsong
 * @date 2019/5/10 18:21
 */
public interface IHandler extends IExecutorApi,Closeable {

    /**
     * Executor 心跳监测及注册
     *
     * @param executor
     */
    void beat(Executor executor);

    default void close() {
    }

    default String name() {
        return this.toString();
    }
}
