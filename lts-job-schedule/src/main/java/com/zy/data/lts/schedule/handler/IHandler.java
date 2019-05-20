package com.zy.data.lts.schedule.handler;

import com.zy.data.lts.core.model.ExecuteRequest;
import com.zy.data.lts.core.model.Executor;

import java.io.Closeable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author chenqingsong
 * @date 2019/5/10 18:21
 */
public interface IHandler extends Closeable {
    /**
     * 异步执行作业
     * @param consumer
     */
    void asyncExec(Consumer<Executor> consumer);

    /**
     * Executor 心跳监测及注册
     * @param executor
     */
    void beat(Executor executor);

    /**
     * 移除指定Executor
     * @param host
     */
    void remove(String host);

    /**
     * 获取指定Executor
     * @param host
     * @return
     */
    Executor getExecutor(String host);

    default void close() { }
}
