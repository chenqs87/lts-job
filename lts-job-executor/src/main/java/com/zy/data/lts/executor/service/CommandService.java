package com.zy.data.lts.executor.service;

import com.google.common.collect.Maps;
import com.zy.data.lts.core.dao.FlowScheduleLogDao;
import com.zy.data.lts.core.entity.FlowScheduleLog;
import com.zy.data.lts.core.model.JobResultRequest;
import com.zy.data.lts.executor.config.ExecutorConfig;
import com.zy.data.lts.executor.model.JobExecuteEvent;
import com.zy.data.lts.executor.model.KillJobEvent;
import com.zy.data.lts.executor.type.IJobTypeHandler;
import com.zy.data.lts.executor.utils.LocalFileLogger;
import com.zy.data.lts.naming.master.AsyncMaster;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author chenqingsong
 * @date 2019/4/9 17:09
 */
@Service
public class CommandService implements ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(CommandService.class);

    private static final String SYS_LOG_FILE = "syslog";

    private static final Object EMPTY_OBJECT = new Object();
    private final ConcurrentHashMap<String, Process> runningTasks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Object> killedTasks = new ConcurrentHashMap<>();

    @Autowired
    AsyncMaster adminService;

    @Autowired
    LogService logService;

    @Autowired
    ExecutorConfig executorConfig;

    @Autowired
    FlowScheduleLogDao flowScheduleLogDao;

    private ApplicationContext applicationContext;

    @EventListener
    public void onApplicationEvent(JobExecuteEvent event) throws IOException {
        Map<String, IJobTypeHandler> jobTypeHandlers = applicationContext.getBeansOfType(IJobTypeHandler.class);
        IJobTypeHandler jobTypeHandler = jobTypeHandlers.get(event.getJobType());

        if (jobTypeHandler == null) {
            String msg = "JobType [" + event.getJobType() + "] is not exist!!!";
            flowScheduleLogDao.insert(new FlowScheduleLog(event.getFlowTaskId(),
                    "Task [" + event.getTaskId() + "]! " + msg));
            throw new IllegalArgumentException(msg);
        }

        String[] command = jobTypeHandler.createCommand(event);
        int ret = execCommand(event, command, jobTypeHandler.getEnv(event));
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
            flowScheduleLogDao.insert(new FlowScheduleLog(event.getFlowTaskId(),
                    "Fail to kill task [" + event.getTaskId() + "]"));
        }
    }

    private void callback(int ret, JobExecuteEvent event) {
        JobResultRequest request = new JobResultRequest(event.getFlowTaskId(), event.getTaskId(), event.getShard());
        if (ret == 0) {
            flowScheduleLogDao.insert(new FlowScheduleLog(event.getFlowTaskId(),
                    "Success to execute task [" + event.getTaskId() + "]"));
            adminService.success(request);
        } else {
            String key = buildKey(event.getFlowTaskId(), event.getTaskId(), event.getShard());
            if (killedTasks.get(key) == EMPTY_OBJECT) {
                killedTasks.remove(key);
            } else {
                flowScheduleLogDao.insert(new FlowScheduleLog(event.getFlowTaskId(),
                        "Fail to execute task [" + event.getTaskId() + "]! Command exec wrong!"));
                adminService.fail(request);
            }
        }
    }

    private int execCommand(JobExecuteEvent event, String[] command, Map<String, Object> env) throws IOException {

        Process process = Runtime.getRuntime().exec(command, createEnv(env));

        String runningKey = buildKey(event.getFlowTaskId(), event.getTaskId(), event.getShard());
        runningTasks.put(runningKey, process);

        int exitValue = -1;
        try (InputStream is = process.getInputStream();
             InputStream error = process.getErrorStream()) {
            LocalFileLogger lfl = logService.createLogger(event, is, error, SYS_LOG_FILE);

            exitValue = process.waitFor();

            lfl.awaitCompletion(5000);

        } catch (InterruptedException ignore) {
        } finally {
            runningTasks.remove(runningKey);
        }

        return exitValue;
    }

    private String[] createEnv(Map<String, Object> paramEnv) {
        Map<String, String> env = Maps.newHashMap(System.getenv());
        env.putAll(executorConfig.getExecuteEnv());
        List<String> ret = new LinkedList<>();
        if(MapUtils.isNotEmpty(env)) {
            env.forEach((k,v) -> ret.add(k + "=" + v));
        }

        if (MapUtils.isNotEmpty(paramEnv)) {
            paramEnv.forEach((k,v) -> ret.add(k + "=" + v));
        }

        return ret.toArray(new String[0]);
    }


    /**
     * executor 强制关闭时，尚未完成的作业不会发生转移，直接出发kill job操作
     * TODO :: executor 强制关闭时，允许作业向Master发起重新发布的请求。
     */
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
                adminService.kill(new JobResultRequest(Integer.parseInt(ids[0]),
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

    /**
     * @return [flowTaskId,taskId,shard]
     */
    public List<Map<String, String>> getRunningTasks(String flowTaskId) {
        return runningTasks.keySet().stream()
                .map(k -> {
                    String[] attrs = k.split("_");
                    if(StringUtils.isNotBlank(flowTaskId) && !flowTaskId.equals(attrs[0])) {
                        return null;
                    }

                    Map<String, String> obj = new HashMap<>(attrs.length);
                    obj.put("flowTaskId", attrs[0]);
                    obj.put("taskId", attrs[1]);
                    obj.put("shard", attrs[2]);
                    return obj;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<Map<String, String>> getRunningTasks() {
        return getRunningTasks(null);
    }
}
