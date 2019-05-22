package com.zy.data.lts.executor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author chenqingsong
 * @date 2019/4/9 17:56
 */
@Configuration
public class ExecutorConfig {
    @Value("${lts.job.executor.dir:/tmp/lts-executor}")
    private String executeRootDir;

    public String getExecuteRootDir() {
        return executeRootDir;
    }

    public void setExecuteRootDir(String executeRootDir) {
        this.executeRootDir = executeRootDir;
    }

    public Path getExecDir(int flowTaskId, int taskId, int shard) throws IOException {
        Path path = Paths.get(executeRootDir,
                String.valueOf(flowTaskId),
                String.valueOf(taskId),
                String.valueOf(shard));

        Files.createDirectories(path);
        return path;
    }

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
