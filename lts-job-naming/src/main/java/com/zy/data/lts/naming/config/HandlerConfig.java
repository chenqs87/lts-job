package com.zy.data.lts.naming.config;

import com.zy.data.lts.naming.handler.LocalHandlerManager;
import com.zy.data.lts.naming.handler.ZkHandlerManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name="lts.server.role", havingValue = "master")
public class HandlerConfig {

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

}
