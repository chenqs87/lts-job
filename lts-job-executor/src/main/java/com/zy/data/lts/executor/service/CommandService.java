package com.zy.data.lts.executor.service;

import com.zy.data.lts.core.api.AdminApi;
import com.zy.data.lts.core.model.JobResultRequest;
import com.zy.data.lts.executor.model.JobExecuteEvent;
import com.zy.data.lts.executor.model.KillJobEvent;
import com.zy.data.lts.executor.type.IJobTypeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author chenqingsong
 * @date 2019/4/9 17:09
 */
@Service
public class CommandService implements ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(CommandService.class);

    private static final Object EMPTY_OBJECT = new Object();
    private final ConcurrentHashMap<String, Process> runningTasks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Object> killedTasks = new ConcurrentHashMap<>();

    @Autowired
    AdminApi adminApi;

    @Autowired
    LogService logService;

    private ApplicationContext applicationContext;

    @EventListener
    public void onApplicationEvent(JobExecuteEvent event) throws IOException {
        Map<String, IJobTypeHandler> jobTypeHandlers = applicationContext.getBeansOfType(IJobTypeHandler.class);
        IJobTypeHandler jobTypeHandler = jobTypeHandlers.get(event.getJobType());

        if (jobTypeHandler == null) {
            throw new IllegalArgumentException("JobType [" + event.getJobType() + "] is not exist!!!");
        }

        String[] command = jobTypeHandler.createCommand(event);
        int ret = execCommand(event, command);
        callback(ret, event);
    }


    @EventListener
    public void onApplicationEvent(KillJobEvent event) {
        String processKey = buildKey(event.getFlowTaskId(), event.getTaskId(), event.getShard());
        try {

            Process process = runningTasks.get(processKey);
            if (process != null) {
                // kill task 之后，作业失败，exitValue != 0, 避免由于主动kill task，造成向Master发起Fail操作
                killedTasks.putIfAbsent(processKey, EMPTY_OBJECT);
                process.destroyForcibly();
            }
        } catch (Exception e) {
            logger.warn("Fail to kill task [{}]!", processKey, e);
        }
    }

    private void callback(int ret, JobExecuteEvent event) {
        JobResultRequest request = new JobResultRequest(event.getFlowTaskId(), event.getTaskId(), event.getShard());
        if (ret == 0) {
            adminApi.success(request);
        } else {
            String key = buildKey(event.getFlowTaskId(), event.getTaskId(), event.getShard());
            if (killedTasks.get(key) == EMPTY_OBJECT) {
                killedTasks.remove(key);
            } else {
                adminApi.fail(request);
            }
        }
    }

    private int execCommand(JobExecuteEvent event, String[] command) throws IOException {
        int exitValue = -1;
        adminApi.start(new JobResultRequest(event.getFlowTaskId(), event.getTaskId(), event.getShard()));
        Process process = Runtime.getRuntime().exec(command);

        String runningKey = buildKey(event.getFlowTaskId(), event.getTaskId(), event.getShard());
        runningTasks.put(runningKey, process);

        try (InputStream is = process.getInputStream();
             InputStream error = process.getErrorStream()) {
            logService.info(event, is);
            logService.error(event, error);
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
                // kill task 之后，作业失败，exitValue != 0, 避免由于主动kill task，造成向Master发起Fail操作
                killedTasks.putIfAbsent(key, EMPTY_OBJECT);
                process.destroyForcibly();
            } catch (Exception e) {
                logger.warn("Fail to kill task [{}]", key);
            } finally {
                String[] ids = key.split("_");
                adminApi.kill(new JobResultRequest(Integer.parseInt(ids[0]),
                        Integer.parseInt(ids[1]), Integer.parseInt(ids[2])));
            }
        });
    }

    private String buildKey(int flowTaskId, int taskId, int shard) {
        return flowTaskId + "_" + taskId + "_" + shard;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
