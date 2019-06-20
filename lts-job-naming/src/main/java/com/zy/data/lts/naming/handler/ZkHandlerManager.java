package com.zy.data.lts.naming.handler;

import com.zy.data.lts.core.tool.SpringContext;
import com.zy.data.lts.naming.config.ServerConfig;
import com.zy.data.lts.naming.zk.ZkClient;
import org.apache.commons.collections.CollectionUtils;
import org.apache.zookeeper.Watcher;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.List;

public class ZkHandlerManager {
    private static final String ZK_LTS_ROOT = "/lts_job/services";
    private static final String ZK_MASTER_ROOT = ZK_LTS_ROOT + "/master";
    private static final String ZK_HANDLER_ROOT = ZK_LTS_ROOT + "/handler";

    @Autowired
    ZkClient zkClient;

    @Autowired
    ServerConfig serverConfig;

    @PostConstruct
    public void init() {
        register();
    }

    private void listenMasters() {
        List<String> hosts = zkClient.getChildren(ZK_MASTER_ROOT, event -> {
            System.out.println(event);
            //监控添加和删除事件
        });
    }

    private void listenHandlers() {
        List<String> handlers = zkClient.getChildren(ZK_HANDLER_ROOT, event -> {
            listenHandlers();
        });

        if(CollectionUtils.isEmpty(handlers)) {
            return;
        }

        handlers.forEach(handler -> {
            SpringContext.getOrCreateBean(handler + AsyncHandler.class.getName(),
                    AsyncHandler.class, handler);

            listenExecutors(handler);
        });
    }

    private void listenExecutors(String handler) {
        List<String> executors = zkClient.getChildren(ZK_HANDLER_ROOT + "/" + handler, event -> {
            if(event.getType() == Watcher.Event.EventType.NodeChildrenChanged) {
                SpringContext.getBeanByName(handler + RoundRobinHandler.class.getName(), RoundRobinHandler.class);
            }

            listenExecutors(handler);
        });

        if(CollectionUtils.isNotEmpty(executors)) {
            executors.forEach(host -> {
                SpringContext.publishEvent(
                        new LtsHandlerChangeEvent(handler, HandlerEventType.NEW ,host));
                listenExecutor(host, handler);
            });
        }
    }

    private void listenExecutor(String host, String handler) {
        zkClient.getData(ZK_HANDLER_ROOT + "/" + handler + "/" + host, event -> {
            if(event.getType() == Watcher.Event.EventType.NodeDeleted) {
                SpringContext.getBeanByName(handler + RoundRobinHandler.class.getName(), RoundRobinHandler.class);
            }

            listenExecutor(host, handler);
        });
    }



    private void register() {
        String path;
        switch (serverConfig.getRole()) {
            case "admin":
                path = ZK_MASTER_ROOT + "/" + serverConfig.getHost();
                zkClient.register(path);
                listenHandlers();
                break;
            case "executor":
                path = ZK_HANDLER_ROOT + "/" + serverConfig.getHandler() + "/" +serverConfig.getHost();
                zkClient.register(path);
                listenMasters();
                break;
            default:
                throw new IllegalArgumentException("[lts.server.role] is must be [admin] or [executor]!");
        }


    }
}
