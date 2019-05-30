package com.zy.data.lts.executor.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * @author chenqingsong
 * @date 2019/4/9 17:56
 */
@Configuration
@ConfigurationProperties(prefix = "lts.server.executor")
public class ExecutorConfig {

    private static final String DEFAULT_EXECUTE_ROOT = "/tmp/lts-executor";
    private String executeRoot;
    private Map<String, String> executeEnv;

    @PostConstruct
    void init() {
        if(StringUtils.isBlank(executeRoot)) {
            executeRoot = DEFAULT_EXECUTE_ROOT;
        }
        System.out.println(executeEnv);
    }

    public void setExecuteRoot(String executeRootDir) {
        this.executeRoot = executeRootDir;
    }

    public Path getExecDir(int flowTaskId, int taskId, int shard) throws IOException {
        Path path = Paths.get(executeRoot,
                String.valueOf(flowTaskId),
                String.valueOf(taskId),
                String.valueOf(shard));

        Files.createDirectories(path);
        return path;
    }

    public String getExecuteRoot() {
        return executeRoot;
    }

    public Map<String, String> getExecuteEnv() {
        return executeEnv;
    }

    public void setExecuteEnv(Map<String, String> executeEnv) {
        this.executeEnv = executeEnv;
    }

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
