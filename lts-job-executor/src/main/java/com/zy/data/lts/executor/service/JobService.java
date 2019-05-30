package com.zy.data.lts.executor.service;

import com.zy.data.lts.core.api.AdminApi;
import com.zy.data.lts.core.dao.FlowDao;
import com.zy.data.lts.core.dao.FlowTaskDao;
import com.zy.data.lts.core.dao.JobDao;
import com.zy.data.lts.core.dao.TaskDao;
import com.zy.data.lts.core.entity.FlowTask;
import com.zy.data.lts.core.entity.Job;
import com.zy.data.lts.core.entity.Task;
import com.zy.data.lts.core.model.ExecuteRequest;
import com.zy.data.lts.core.model.JobResultRequest;
import com.zy.data.lts.core.model.KillTaskRequest;
import com.zy.data.lts.executor.config.ExecutorConfig;
import com.zy.data.lts.executor.model.JobExecuteEvent;
import com.zy.data.lts.executor.model.KillJobEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static com.zy.data.lts.core.config.ThreadPoolsConfig.EXECUTOR_THREAD_POOL;

/**
 * @author chenqingsong
 * @date 2019/4/9 17:37
 */
@Service
public class JobService implements ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(JobService.class);

    @Autowired
    AdminApi adminService;

    @Autowired
    FlowTaskDao flowTaskDao;

    @Autowired
    FlowDao flowDao;

    @Autowired
    JobDao jobDao;

    @Autowired
    TaskDao taskDao;

    @Autowired
    ExecutorConfig executorConfig;
    private ApplicationContext applicationContext;

    private void doExec(ExecuteRequest req) {
        try {
            Task task = taskDao.findById(req.getFlowTaskId(), req.getTaskId());
            Job job = jobDao.findById(task.getJobId());

            FlowTask flowTask = flowTaskDao.findById(task.getFlowTaskId());
            Path output = createOutputDir(req, job);

            String params = flowTask.getParams();

            JobExecuteEvent event = new JobExecuteEvent(task, output, params, job.getJobType());

            applicationContext.publishEvent(event);
        } catch (Exception e) {
            logger.error("Fail to execute task. task info : {}", req, e);
            JobResultRequest jrr = new JobResultRequest(req.getFlowTaskId(), req.getTaskId(), req.getShard());
            adminService.fail(jrr);
        }
    }

    @Async(EXECUTOR_THREAD_POOL)
    public void exec(ExecuteRequest req) {
        try {
            doExec(req);
        } catch (Exception e) {
            logger.error("Fail to execute task [{}]", req, e);
            adminService.fail(new JobResultRequest(req.getFlowTaskId(), req.getTaskId(), req.getShard()));
        }
    }

    public void killTask(KillTaskRequest req) {
        applicationContext.publishEvent(new KillJobEvent(req.getFlowTaskId(), req.getTaskId(), req.getShard()));
    }

    private Path createOutputDir(ExecuteRequest req, Job job) throws IOException {
        Path root = executorConfig.getExecDir(req.getFlowTaskId(), req.getTaskId(), req.getShard());

        String content = job.getContent();
        Path execFile = Paths.get(root.toString(), "exec");

        execFile.toFile().createNewFile();

        try (InputStream inputStream = new ByteArrayInputStream(content.getBytes())) {
            Files.copy(inputStream, execFile, StandardCopyOption.REPLACE_EXISTING);
        }

        return root;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
