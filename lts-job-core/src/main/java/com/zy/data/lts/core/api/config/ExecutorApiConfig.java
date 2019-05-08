package com.zy.data.lts.core.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author chenqingsong
 * @date 2019/4/1 13:52
 */
@Configuration
@ConditionalOnProperty(name = "lts.server.role", havingValue = "executor")
public class ExecutorApiConfig {

    @Value("${lts.server.executor.adminUrls}")
    private List<String> adminUrls;

    @Value("${lts.server.executor.handler.name}")
    private String handler;

    @Value("${lts.server.executor.handler.host}")
    private String host;

    public List<String> getAdminUrls() {
        return adminUrls;
    }

    public void setAdminUrls(List<String> adminUrls) {
        this.adminUrls = adminUrls;
    }

    public String getHandler() {
        return handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
