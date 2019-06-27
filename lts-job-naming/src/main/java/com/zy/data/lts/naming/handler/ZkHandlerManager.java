package com.zy.data.lts.naming.handler;

import com.zy.data.lts.core.tool.SpringContext;
import com.zy.data.lts.naming.zk.ReconnectCallback;
import com.zy.data.lts.naming.zk.SessionConnectionListener;
import com.zy.data.lts.naming.zk.ZkClient;
import org.apache.commons.collections.CollectionUtils;
import org.apache.zookeeper.Watcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;

import javax.annotation.PostConstruct;
import java.util.List;

import static com.zy.data.lts.naming.zk.ZkConfiguration.ZK_HANDLER_ROOT;
import static com.zy.data.lts.naming.zk.ZkConfiguration.ZK_MASTER_ROOT;

public class ZkHandlerManager {


    @Autowired
    ZkClient zkClient;

    @Value("${lts.server.host}")
    private String host;

    @PostConstruct
    public void init() {
        reInit();

    }

    public void reInit() {
        String path = ZK_MASTER_ROOT + "/" + host;
        zkClient.register(path);
        zkClient.addReconnectListener(new SessionConnectionListener(path, this::listenHandlers));
        listenHandlers();
    }

    private void listenHandlers() {
        List<String> handlers = zkClient.getChildren(ZK_HANDLER_ROOT, event -> {
            if(event.getState() == Watcher.Event.KeeperState.Expired) {
                reInit();
            }
            listenHandlers();
        });

        if(CollectionUtils.isEmpty(handlers)) {
            return;
        }

        handlers.forEach(handler -> {
            SpringContext.getOrCreateBean(handler + AsyncHandler.class.getSimpleName(),
                    AsyncHandler.class, handler);

            listenExecutors(handler);
        });
    }

    private void listenExecutors(String handler) {
        List<String> executors = zkClient.getChildren(ZK_HANDLER_ROOT + "/" + handler, event -> {
            listenExecutors(handler);
            if(event.getType() == Watcher.Event.EventType.NodeChildrenChanged) {
                SpringContext.getBeanByName(handler + RoundRobinHandler.class.getSimpleName(), RoundRobinHandler.class);
            }

        });

        if(CollectionUtils.isNotEmpty(executors)) {
            executors.forEach(host -> {
                listenExecutor(host, handler);
                SpringContext.publishEvent(
                        new LtsHandlerChangeEvent(handler, HandlerEventType.NEW ,host));

            });
        }
    }

    private void listenExecutor(String host, String handler) {
        zkClient.checkExist(ZK_HANDLER_ROOT + "/" + handler + "/" + host, event -> {
            listenExecutor(host, handler);
            if(event.getType() == Watcher.Event.EventType.NodeDeleted) {
                SpringContext.getBeanByName(handler + RoundRobinHandler.class.getSimpleName(), RoundRobinHandler.class);
            }
        });
    }

    @EventListener
    public void handle(ExecutorUnConnectedEvent event) {
        SpringContext.publishEvent(
                new LtsHandlerChangeEvent(event.getExecutor().getHandler(), HandlerEventType.DELETE ,event.getExecutor().getHost()));
    }


}
