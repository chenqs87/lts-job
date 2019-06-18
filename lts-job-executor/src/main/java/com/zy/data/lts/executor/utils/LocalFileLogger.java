package com.zy.data.lts.executor.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class LocalFileLogger {

    private OutputStream output;
    private List<LogThread> logThreads;

    public LocalFileLogger(Path outputFile, List<InputStream> inputs) throws IOException {
        this.output = Files.newOutputStream(outputFile);
        logThreads = new ArrayList<>(inputs.size());
        inputs.forEach(is -> {
            LogThread lt = new LogThread(this.output, is);
            logThreads.add(lt);
            lt.start();
        });
    }

    public void awaitCompletion(long time) {

        logThreads.forEach( lt -> {
            try {
                lt.join(time);
            } catch (InterruptedException ignore){}
        });

        try {
            if (output != null) {
                output.close();
            }
        } catch (IOException ignore) {}
    }
}
