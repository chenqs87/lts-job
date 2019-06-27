package com.zy.data.lts.naming.zk;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class ZkClient {
    private static final Logger logger = LoggerFactory.getLogger(ZkClient.class);

    private CuratorFramework client;
    private String zookeeperServer;
    private int sessionTimeoutMs;
    private int connectionTimeoutMs;
    private int baseSleepTimeMs;
    private int maxRetries;

    public void setZookeeperServer(String zookeeperServer) {
        this.zookeeperServer = zookeeperServer;
    }

    public String getZookeeperServer() {
        return zookeeperServer;
    }

    public void setSessionTimeoutMs(int sessionTimeoutMs) {
        this.sessionTimeoutMs = sessionTimeoutMs;
    }

    public int getSessionTimeoutMs() {
        return sessionTimeoutMs;
    }

    public void setConnectionTimeoutMs(int connectionTimeoutMs) {
        this.connectionTimeoutMs = connectionTimeoutMs;
    }

    public int getConnectionTimeoutMs() {
        return connectionTimeoutMs;
    }

    public void setBaseSleepTimeMs(int baseSleepTimeMs) {
        this.baseSleepTimeMs = baseSleepTimeMs;
    }

    public int getBaseSleepTimeMs() {
        return baseSleepTimeMs;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void init() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(baseSleepTimeMs, maxRetries);
        client = CuratorFrameworkFactory.builder().connectString(zookeeperServer).retryPolicy(retryPolicy)
                .sessionTimeoutMs(sessionTimeoutMs).connectionTimeoutMs(connectionTimeoutMs).build();
        client.start();
    }

    public void stop() {
        client.close();
    }

    public void register(String path) {
        try {
            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);
        } catch (Exception e) {
            logger.error("Fail to register path [{}]", path, e);
            throw new IllegalStateException(e);
        }
    }

    public void addReconnectListener(SessionConnectionListener listener) {
        client.getConnectionStateListenable().addListener(listener);
    }

    public List<String> getChildren(String path, final Watcher watcher) {
        List<String> childrenList = new ArrayList<>();
        try {
            childrenList = client.getChildren().usingWatcher(watcher).forPath(path);
        } catch (Exception e) {
            logger.error("Fail to get children for [{}]", path, e);
            throw new IllegalStateException(e);
        }
        return childrenList;
    }

    public void checkExist(String path, final Watcher watcher) {
        try {
            client.checkExists().usingWatcher(watcher).forPath(path);
        } catch (Exception e) {
            logger.error("Fail to get data for [{}]", path, e);
            throw new IllegalStateException(e);
        }
    }

    public List<String> getChildren(String path) {
        List<String> childrenList = new ArrayList<>();
        try {
            childrenList = client.getChildren().forPath(path);
        } catch (Exception e) {
            logger.error("Fail to get children for [{}]", path, e);
            throw new IllegalStateException(e);
        }
        return childrenList;
    }
}