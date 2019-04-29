package com.zy.data.lts.core.model;

/**
 * @author chenqingsong
 * @date 2019/4/9 16:21
 */
public class BeatInfoRequest {
    private String handler;
    private float cpuIdle;
    private float memFree;
    private int port;
    private String host;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getHandler() {
        return handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }

    public float getCpuIdle() {
        return cpuIdle;
    }

    public void setCpuIdle(float cpuIdle) {
        this.cpuIdle = cpuIdle;
    }

    public float getMemFree() {
        return memFree;
    }

    public void setMemFree(float memFree) {
        this.memFree = memFree;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

}
