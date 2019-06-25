package com.zy.data.lts.naming.config;


import com.zy.data.lts.naming.master.LocalMasterManager;
import com.zy.data.lts.naming.master.ZkMasterManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name="lts.server.role", havingValue = "executor")
public class MasterConfig {
    @Bean("masterManager")
    @ConditionalOnProperty(name = "lts.server.naming", havingValue = "local")
    public LocalMasterManager getLocalMasterManager() {
        return new LocalMasterManager();
    }

    @Bean(value = "masterManager", initMethod = "init")
    @ConditionalOnProperty(name = "lts.server.naming", havingValue = "zk")
    public ZkMasterManager getZkMasterManager() {
        return new ZkMasterManager();
    }

}
