package com.zy.data.lts.naming.master;

import com.zy.data.lts.core.tool.SpringContext;
import com.zy.data.lts.naming.zk.SessionConnectionListener;
import com.zy.data.lts.naming.zk.ZkClient;
import org.apache.commons.collections.CollectionUtils;
import org.apache.zookeeper.Watcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Set;

import static com.zy.data.lts.naming.zk.ZkConfiguration.ZK_HANDLER_ROOT;
import static com.zy.data.lts.naming.zk.ZkConfiguration.ZK_MASTER_ROOT;

public class ZkMasterManager{

    @Value("${lts.server.handler}")
    private String handler;

    @Value("${lts.server.host}")
    private String host;

    @Autowired
    ZkClient zkClient;

    private volatile int masters = 0;

    public void init() {
        String path = ZK_HANDLER_ROOT + "/" + handler + "/" + host;
        zkClient.register(path);
        zkClient.addReconnectListener(new SessionConnectionListener(path, this::listenMasters));
        listenMasters();
    }

    private void listenMasters() {
        List<String> hosts = zkClient.getChildren(ZK_MASTER_ROOT, event -> {
            listenMasters();
        });

        if(CollectionUtils.isEmpty(hosts)) {
            this.masters = 0;
            return;
        }

        this.masters = hosts.size();
        hosts.forEach(host -> {
            SpringContext.publishEvent(new LtsMasterChangeEvent(host, MasterEventType.NEW));
            listenMaster(host);
        });
    }

    private void listenMaster(String host) {
        zkClient.checkExist(ZK_MASTER_ROOT + "/" + host, event -> {
            listenMaster(host);
            if(event.getType() == Watcher.Event.EventType.NodeDeleted) {
                SpringContext.publishEvent(new LtsMasterChangeEvent(host, MasterEventType.DELETE));
            }
        });
    }

}
