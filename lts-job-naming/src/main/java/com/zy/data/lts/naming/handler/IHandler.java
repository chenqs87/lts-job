package com.zy.data.lts.naming.handler;

import com.zy.data.lts.core.api.IExecutor;

import java.io.Closeable;

public interface IHandler extends IExecutor, Closeable {

    default void close() { }

    default String name() {
        return this.toString();
    }

    int getExecutorSize();
}
