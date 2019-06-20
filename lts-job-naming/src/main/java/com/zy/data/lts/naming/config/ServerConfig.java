package com.zy.data.lts.naming.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "lts.server")
public class ServerConfig {
    private String role;
    private String host;
    private String naming;
    private String handler;

    public String getHandler() {
        return handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getNaming() {
        return naming;
    }

    public void setNaming(String naming) {
        this.naming = naming;
    }
}
