package com.zy.data.lts.executor.service;

import com.zy.data.lts.core.api.AdminApi;
import com.zy.data.lts.core.model.JobResultRequest;
import com.zy.data.lts.executor.model.JobExecuteEvent;
import com.zy.data.lts.executor.model.PythonEvent;
import com.zy.data.lts.executor.model.ShellEvent;
import com.zy.data.lts.executor.model.ZipEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author chenqingsong
 * @date 2019/4/9 17:09
 */
@Service
public class CommandService {

    @Autowired
    AdminApi adminApi;

    @Autowired
    LogService logService;

    @EventListener
    public void onApplicationEvent(ShellEvent event) throws IOException {
        int ret = execCommand(event, "sh", event.getOutput().toString() + "/exec");
        callback(ret, event);
    }

    @EventListener
    public void onApplicationEvent(PythonEvent event) throws IOException {
        int ret = execCommand(event, "python",event.getOutput().toString() + "/exec");
        callback(ret, event);
    }

    @EventListener
    public void onApplicationEvent(ZipEvent event) throws IOException {
        int ret = execCommand(event, "sh", event.getOutput().toString() + "/exec");
        callback(ret, event);
    }

    private void callback(int ret, JobExecuteEvent event) {
        JobResultRequest request = new JobResultRequest(event.getFlowTaskId(), event.getTaskId(), event.getShard());
        if(ret == 0) {
            adminApi.success(request);
        } else {
            adminApi.fail(request);
        }
    }

    private int execCommand(JobExecuteEvent event, String command, String file) throws IOException {
        int exitValue = -1;
        adminApi.start(new JobResultRequest(event.getFlowTaskId(), event.getTaskId(), event.getShard()));
        Process process = Runtime.getRuntime().exec(
                new String[]{command, file, String.valueOf(event.getShard()), event.getParams() == null ? "" : event.getParams()});

        try(InputStream is = process.getInputStream()) {
            logService.info(event, is);
            process.waitFor();
            exitValue = process.exitValue();
        } catch (InterruptedException ignore) { }

        return exitValue;
    }
}
