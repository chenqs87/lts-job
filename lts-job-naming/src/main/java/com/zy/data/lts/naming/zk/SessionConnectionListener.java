package com.zy.data.lts.naming.zk;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class SessionConnectionListener implements ConnectionStateListener {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String path;
    private ReconnectCallback callback;

    public SessionConnectionListener(String path, ReconnectCallback callback) {
        this.path = path;
        this.callback = callback;
    }

    @Override
    public void stateChanged(CuratorFramework curatorFramework, ConnectionState connectionState){
        if(connectionState == ConnectionState.LOST){
            logger.error("[负载均衡失败]zk session超时");
            while(true){
                try {
                    if(curatorFramework.getZookeeperClient().blockUntilConnectedOrTimedOut()) {
                        curatorFramework.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);
                        logger.info("[负载均衡修复]重连zk成功");
                        callback.callback();
                        break;
                    }
                } catch (InterruptedException e) {
                    break;
                } catch (Exception e){ }
            }
        }
    }
}