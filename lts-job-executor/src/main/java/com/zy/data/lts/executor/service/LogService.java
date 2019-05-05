package com.zy.data.lts.executor.service;

import com.zy.data.lts.core.model.LogQueryRequest;
import com.zy.data.lts.executor.config.ExecutorConfig;
import com.zy.data.lts.executor.model.JobExecuteEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author chenqingsong
 * @date 2019/4/10 10:20
 */

@Service
public class LogService {


    //private final Map<String, FileOutputStream> files = new ConcurrentHashMap<>();

    @Autowired
    private ExecutorConfig executorConfig;


    /*public void info(JobExecuteEvent event, String log) throws IOException {
        String key = buildKey(event.getFlowTaskId(), event.getTaskId(), event.getShard());
        Path root = executorConfig.getExecDir(event.getFlowTaskId(), event.getTaskId(), event.getShard());
        files.computeIfAbsent(key, f -> newFileOutputStream(root));
    }*/

    public void info(JobExecuteEvent event, InputStream is) throws IOException {
        Path root = executorConfig.getExecDir(event.getFlowTaskId(), event.getTaskId(), event.getShard());
        Path logFile = newFileOutput(root);
        if(logFile != null) {
            Files.deleteIfExists(logFile);
            Files.copy(is, logFile);
        }
    }

    public void queryLog(LogQueryRequest request, HttpServletResponse response) throws IOException {

        Path root = executorConfig.getExecDir(request.getFlowTaskId(), request.getTaskId(), request.getShard());
        Path path = newFileOutput(root);
        File file = path.toFile();
        long length = file.length() - request.getOffset();
        response.setHeader("FileSize", String.valueOf(length));

        try(FileInputStream fis = new FileInputStream(file);
            FileChannel channel = fis.getChannel()) {
            WritableByteChannel output = Channels.newChannel(response.getOutputStream());
            channel.transferTo(request.getOffset(), length, output);
        }
    }

    public void queryLog(Integer flowTaskId, Integer taskId, Integer shard, HttpServletResponse response) throws IOException {
        int offset = 0;
        Path root = executorConfig.getExecDir(flowTaskId, taskId, shard);
        Path path = newFileOutput(root);
        File file = path.toFile();
        long length = file.length() - offset;
        response.setHeader("FileSize", String.valueOf(length));

        try(FileInputStream fis = new FileInputStream(file);
            FileChannel channel = fis.getChannel()) {
            WritableByteChannel output = Channels.newChannel(response.getOutputStream());
            channel.transferTo(offset, length, output);
        }
    }

    private Path newFileOutput(Path rootPah) {
        try {
            Path logFile = buildOutputPath(rootPah);

            if(!Files.exists(logFile)) {
                Files.createFile(logFile);
            }

            return logFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Path buildOutputPath(Path root) {
        return Paths.get(root.toString(), "syslog.log");
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

    private List<String> readLogs(LogQueryRequest request) throws IOException {
        Path root = executorConfig.getExecDir(request.getFlowTaskId(), request.getTaskId(), request.getShard());
        Path logFile = buildOutputPath(root);
        BufferedReader reader = Files.newBufferedReader(logFile);


        return null;
    }

    public void finish(int flowTaskId, int taskId, int shard) {

    }

    public String buildKey(int flowTaskId, int taskId, int shard) {
        return flowTaskId + "_" + taskId + "_" + shard;
    }

}
