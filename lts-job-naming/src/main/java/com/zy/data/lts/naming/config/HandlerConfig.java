package com.zy.data.lts.naming.config;

import com.zy.data.lts.naming.handler.LocalHandlerManager;
import com.zy.data.lts.naming.handler.ZkHandlerManager;
import com.zy.data.lts.naming.zk.ZkClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import static com.zy.data.lts.naming.zk.ZkConfiguration.ZK_MASTER_ROOT;

@Component
@ConditionalOnProperty(name="lts.server.role", havingValue = "master")
public class HandlerConfig {

    @Value("${lts.server.naming}")
    private String naming;

    @Autowired(required = false)
    private ZkClient zkClient;

    @Bean
    @ConditionalOnProperty(name = "lts.server.naming", havingValue = "local")
    public LocalHandlerManager getLocalHandlerManager() {
        return new LocalHandlerManager();
    }

    @Bean(initMethod = "init")
    @ConditionalOnProperty(name = "lts.server.naming", havingValue = "zk")
    public ZkHandlerManager getZkHandlerManager() {
        return new ZkHandlerManager();
    }

    public int getMasters() {
        switch (naming) {
            case "local" : return 1;
            case "zk": return zkClient.getChildren(ZK_MASTER_ROOT).size();
            default: return 0;
        }
    }

}
