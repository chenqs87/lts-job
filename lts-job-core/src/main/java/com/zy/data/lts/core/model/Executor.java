package com.zy.data.lts.core.model;

import com.zy.data.lts.core.api.IExecutorApi;
import com.zy.data.lts.core.entity.Task;

import java.util.HashMap;
import java.util.Map;

/**
 * @author chenqingsong
 * @date 2019/4/11 11:21
 */
public class Executor implements IExecutorApi{
    private static final long TIME_OUT = 30 * 60 * 1000;
    private final Map<String, Task> runningTasks = new HashMap<>();
    private IExecutorApi api;
    private float memFree;
    private float cpuIdle;
    private String host;
    private volatile long lastUpdateTime = System.currentTimeMillis();
    private volatile String handler;

    public IExecutorApi getApi() {
        return api;
    }

    public void setApi(IExecutorApi api) {
        this.api = api;
    }

    public float getMemFree() {
        return memFree;
    }

    public void setMemFree(float memFree) {
        this.memFree = memFree;
    }

    public float getCpuIdle() {
        return cpuIdle;
    }

    public void setCpuIdle(float cpuIdle) {
        this.cpuIdle = cpuIdle;
    }

    public Map<String, Task> getRunningTasks() {
        return runningTasks;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public boolean isActive() {
        return System.currentTimeMillis() - lastUpdateTime < TIME_OUT;
    }

    public String getHandler() {
        return handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }

    @Override
    public void execute(ExecuteRequest request) {
        api.execute(request);
    }

    @Override
    public void kill(KillTaskRequest request) {
        api.kill(request);
    }
}
