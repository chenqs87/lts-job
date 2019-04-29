package com.zy.data.lts.executor.service;

import com.zy.data.lts.executor.config.ExecutorConfig;
import com.zy.data.lts.executor.model.JobExecuteEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author chenqingsong
 * @date 2019/4/10 10:20
 */

@Service
public class LogService {


    private final Map<String, FileOutputStream> files = new ConcurrentHashMap<>();

    @Autowired
    private ExecutorConfig executorConfig;


    public void info(JobExecuteEvent event, String log) throws IOException {
        String key = buildKey(event.getFlowTaskId(), event.getTaskId(), event.getShard());
        Path root = executorConfig.getExecDir(event.getFlowTaskId(), event.getTaskId(), event.getShard());
        files.computeIfAbsent(key, f -> newFileOutputStream(root));
    }

    public void info(JobExecuteEvent event, InputStream is) throws IOException {
        Path root = executorConfig.getExecDir(event.getFlowTaskId(), event.getTaskId(), event.getShard());

        Path logFile = newFileOutput(root);
        if(logFile != null) {
            Files.deleteIfExists(logFile);
            Files.copy(is, logFile);
        }
    }

    private Path newFileOutput(Path rootPah) {
        try {
            Path logFile = Paths.get(rootPah.toString(), "syslog.log");

            if(!Files.exists(logFile)) {
                Files.createFile(logFile);
            }

            return logFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private FileOutputStream newFileOutputStream(Path rootPah) {
        try {
            Path logFile = newFileOutput(rootPah);
            if(logFile == null) {
                return null;
            }
            return new FileOutputStream(logFile.toFile());
        } catch (IOException e) {
            // print log
            e.printStackTrace();
            return null;
        }
    }

    public void finish(int flowTaskId, int taskId, int shard) {

    }

    public String buildKey(int flowTaskId, int taskId, int shard) {
        return flowTaskId + "_" + taskId + "_" + shard;
    }

}
