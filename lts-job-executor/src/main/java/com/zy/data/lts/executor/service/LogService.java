package com.zy.data.lts.executor.service;

import com.zy.data.lts.executor.config.ExecutorConfig;
import com.zy.data.lts.executor.model.JobExecuteEvent;
import com.zy.data.lts.executor.utils.LocalFileLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * @author chenqingsong
 * @date 2019/4/10 10:20
 */

@Service
public class LogService {
    private static final Logger logger = LoggerFactory.getLogger(LogService.class);

    @Autowired
    private ExecutorConfig executorConfig;

    public LocalFileLogger createLogger(JobExecuteEvent event, InputStream is, InputStream error ,String logName) throws IOException {
        Path root = executorConfig.getExecDir(event.getFlowTaskId(), event.getTaskId(), event.getShard());
        Path logFile = newFileOutput(root, logName);
        Files.deleteIfExists(logFile);
        return new LocalFileLogger(logFile, Arrays.asList(is, error));
    }




    public void queryLog(Integer flowTaskId, Integer taskId, Integer shard, String logName, HttpServletResponse response) throws IOException {
        int offset = 0;
        Path root = executorConfig.getExecDir(flowTaskId, taskId, shard);
        Path path = newFileOutput(root, logName);
        File file = path.toFile();
        long length = file.length() - offset;
        response.setHeader("FileSize", String.valueOf(length));
        try (FileInputStream fis = new FileInputStream(file);
             FileChannel channel = fis.getChannel()) {
            WritableByteChannel output = Channels.newChannel(response.getOutputStream());
            channel.transferTo(offset, length, output);
        }
    }

    private Path newFileOutput(Path rootPah, String fileName) {
        Path logFile = buildOutputPath(rootPah, fileName);
        try {

            if (!Files.exists(logFile)) {
                Files.createFile(logFile);
            }

            return logFile;
        } catch (IOException e) {
            throw new IllegalStateException("Fail to create output path ["+ logFile +"]");
        }
    }

    private Path buildOutputPath(Path root, String fileName) {
        return Paths.get(root.toString(), fileName);
    }


}
