package com.zy.data.lts.naming.handler;

import com.zy.data.lts.core.api.IExecutorApi;

import java.io.Closeable;

public interface IHandler extends IExecutorApi, Closeable {

    default void close() { }

    default String name() {
        return this.toString();
    }
}
