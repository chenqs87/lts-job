package com.zy.data.lts.executor.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class LogThread extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(LogThread.class);

    private final OutputStream os;
    private final InputStream is;

    public LogThread(OutputStream os, InputStream is) {
        this.os = os;
        this.is = is;
    }

    public void run() {
        try {
            BufferedReader bis = new BufferedReader(new InputStreamReader(is));

            String str;
            while ((str = bis.readLine()) != null) {
                synchronized (os) {
                    os.write(str.getBytes());
                    os.write('\n');
                    os.flush();
                }
            }
        } catch (Exception e) {
            logger.error("Fail to write log to syslog!", e);
        }

    }
}