package com.zy.data.lts.executor.service;

import com.zy.data.lts.core.api.AdminApi;
import com.zy.data.lts.core.model.ExecuteRequest;
import com.zy.data.lts.core.model.JobResultRequest;
import com.zy.data.lts.executor.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;

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

    private final ConcurrentHashMap<String, Process> runningTasks = new ConcurrentHashMap<>();

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

    @EventListener
    public void onApplicationEvent(KillJobEvent event) {
        try {
            String processKey = buildKey(event.getFlowTaskId(), event.getTaskId(), event.getShard());
            Process process = runningTasks.get(processKey);
            if(process != null) {
                // TODO::  Kill 作业会触发作业失败事件 adminApi.fail(request);
                process.destroyForcibly();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // todo 添加日志
        }
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

        String runningKey = buildKey(event.getFlowTaskId(), event.getTaskId(), event.getShard());
        runningTasks.put(runningKey, process);

        try(InputStream is = process.getInputStream()) {
            logService.info(event, is);
            process.waitFor();
            exitValue = process.exitValue();
        } catch (InterruptedException ignore) {
        } finally {
            runningTasks.remove(runningKey);
        }


        return exitValue;
    }

    @PreDestroy
    public void destroy() {
        runningTasks.forEach((key, process) -> {
            try {
                process.destroyForcibly();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // TODO::  Kill 作业会触发作业失败事件 adminApi.fail(request);
                String[] ids = key.split("_");
                adminApi.kill(new JobResultRequest(Integer.parseInt(ids[0]),
                        Integer.parseInt(ids[1]), Integer.parseInt(ids[2])));
            }
        });
    }

    private String buildKey(int flowTaskId, int taskId, int shard) {
        return flowTaskId + "_" + taskId + "_" + shard;
    }
}
