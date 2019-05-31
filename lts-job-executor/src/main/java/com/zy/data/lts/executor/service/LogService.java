package com.zy.data.lts.executor.service;

import com.zy.data.lts.executor.config.ExecutorConfig;
import com.zy.data.lts.executor.model.JobExecuteEvent;
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

/**
 * @author chenqingsong
 * @date 2019/4/10 10:20
 */

@Service
public class LogService {

    @Autowired
    private ExecutorConfig executorConfig;

    public void write(JobExecuteEvent event, InputStream is, String logName) throws IOException {
        Path root = executorConfig.getExecDir(event.getFlowTaskId(), event.getTaskId(), event.getShard());
        Path logFile = newFileOutput(root, logName);

        if (logFile != null) {
            Files.deleteIfExists(logFile);
            copy(is, logFile);
        }
    }

    private void copy(InputStream is, Path logFile) throws IOException {
        try (BufferedReader bis = new BufferedReader(new InputStreamReader(is));
             OutputStream fos = Files.newOutputStream(logFile)) {
            String str;
            while ((str = bis.readLine()) != null) {
                fos.write(str.getBytes());
                fos.write('\n');
                fos.flush();
            }
        }
    }

    public void queryLog(Integer flowTaskId, Integer taskId, Integer shard, String logName, HttpServletResponse response) throws IOException {
        int offset = 0;
        Path root = executorConfig.getExecDir(flowTaskId, taskId, shard);
        Path path = newFileOutput(root, logName);
        File file = path.toFile();
        long length = file.length() - offset;
        response.setHeader("FileSize", String.valueOf(length));
        response.getOutputStream().print("--------------------------------------------------\n");
        response.getOutputStream().print(logName+" output is :\n");
        try (FileInputStream fis = new FileInputStream(file);
             FileChannel channel = fis.getChannel()) {
            WritableByteChannel output = Channels.newChannel(response.getOutputStream());
            channel.transferTo(offset, length, output);
        }
    }

    private Path newFileOutput(Path rootPah, String fileName) {
        try {
            Path logFile = buildOutputPath(rootPah, fileName);

            if (!Files.exists(logFile)) {
                Files.createFile(logFile);
            }

            return logFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Path buildOutputPath(Path root, String fileName) {
        return Paths.get(root.toString(), fileName);
    }




}
