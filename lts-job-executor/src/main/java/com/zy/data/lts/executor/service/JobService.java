package com.zy.data.lts.executor.service;

import com.zy.data.lts.core.JobType;
import com.zy.data.lts.core.dao.FlowDao;
import com.zy.data.lts.core.dao.FlowTaskDao;
import com.zy.data.lts.core.dao.JobDao;
import com.zy.data.lts.core.dao.TaskDao;
import com.zy.data.lts.core.entity.FlowTask;
import com.zy.data.lts.core.entity.Job;
import com.zy.data.lts.core.entity.Task;
import com.zy.data.lts.core.model.ExecuteRequest;
import com.zy.data.lts.executor.config.ExecutorConfig;
import com.zy.data.lts.executor.model.*;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author chenqingsong
 * @date 2019/4/9 17:37
 */
@Service
public class JobService implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    private final ConcurrentHashMap<String, String> runningTasks = new ConcurrentHashMap<>();

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

    public void exec(ExecuteRequest req) throws IOException {
        Task task = taskDao.findById(req.getFlowTaskId(), req.getTaskId());
        Job job = jobDao.findById(task.getJobId());

        FlowTask flowTask = flowTaskDao.findById(task.getFlowTaskId());
        JobType jobType = JobType.parse(job.getJobType());
        Path output = createOutputDir(req, job, jobType);

        String params = flowTask.getParams();

        JobExecuteEvent event = null;

        switch (jobType) {
            case shell: event = new ShellEvent(req.getFlowTaskId(), req.getTaskId(), req.getShard(),output, params); break;
            case python: event = new PythonEvent(req.getFlowTaskId(), req.getTaskId(), req.getShard(),output, params); break;
            case zip: event = new ZipEvent(req.getFlowTaskId(), req.getTaskId(), req.getShard(),output, params); break;
        }

        applicationContext.publishEvent(event);
    }

    private Path createOutputDir(ExecuteRequest req, Job job, JobType jobType) throws IOException {
        Path root = executorConfig.getExecDir(req.getFlowTaskId(), req.getTaskId(), req.getShard());

        String content = job.getContent();
        Path execFile = Paths.get(root.toString(), "exec");

       // Files.createFile(execFile);
        boolean success = execFile.toFile().createNewFile();

        try(InputStream inputStream = new ByteArrayInputStream(content.getBytes())) {
            Files.copy(inputStream, execFile, StandardCopyOption.REPLACE_EXISTING);
        }

        if(jobType == JobType.zip) {
            // TODO 解压缩
        }

        return root;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
