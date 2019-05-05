package com.zy.data.lts.schedule.service;

import com.zy.data.lts.core.dao.FlowDao;
import com.zy.data.lts.core.dao.FlowTaskDao;
import com.zy.data.lts.core.dao.JobDao;
import com.zy.data.lts.core.dao.TaskDao;
import com.zy.data.lts.core.entity.Flow;
import com.zy.data.lts.core.entity.FlowTask;
import com.zy.data.lts.core.entity.Job;
import com.zy.data.lts.core.entity.Task;
import com.zy.data.lts.schedule.timer.JobScheduler;
import com.zy.data.lts.schedule.trigger.JobTrigger;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @author chenqingsong
 * @date 2019/3/29 11:56
 */
@Service
public class JobService {

    @Autowired
    JobTrigger jobTrigger;

    @Autowired
    JobScheduler jobScheduler;

    @Autowired
    FlowDao flowDao;

    @Autowired
    JobDao jobDao;

    @Autowired
    FlowTaskDao flowTaskDao;

    @Autowired
    TaskDao taskDao;

    /**
     * 创建工作流
     * @param flow
     * @throws Exception
     */
    public Flow createFlow(Flow flow) {
        flow.setCreateTime(new Date());
        flow.setCreateUser(0);
        flowDao.insert(flow);
        flow.setId(flowDao.getId());
        return flow;
    }

    public void deleteFlow(int flowId) {
        flowDao.delete(flowId);
    }

    public Flow updateFlow(Flow flow) {
        Flow dbFlow = flowDao.findById(flow.getId());

        //更新工作流基本信息
        if(StringUtils.isNotBlank(flow.getName())) {
            dbFlow.setName(flow.getName());
            dbFlow.setCron(flow.getCron());
            dbFlow.setParams(flow.getParams());
        }

        // 更新工作流配置信息
        if(StringUtils.isNotBlank(flow.getFlowEditorInfo())) {
            dbFlow.setFlowConfig(flow.getFlowConfig());
            dbFlow.setFlowEditorInfo(flow.getFlowEditorInfo());
        }

        flowDao.update(dbFlow);

        return flow;
    }

    @Transactional
    public Job createJob(Job job) {
        job.setCreateTime(new Date());
        job.setCreateUser(0);
        jobDao.insert(job);
        job.setId(jobDao.getId());
        return job;
    }

    public void deleteJob(int jobId) {
        jobDao.delete(jobId);
    }

    public Job updateJob(Job job) {
        Job dbJob = jobDao.findById(job.getId());

        if(StringUtils.isNotBlank(job.getName())) {
            dbJob.setName(job.getName());
            dbJob.setConfig(job.getConfig());
            dbJob.setHandler(job.getHandler());
            dbJob.setJobType(job.getJobType());
            dbJob.setShardType(job.getShardType());
        }

        if(StringUtils.isNotBlank(job.getContent())) {
            dbJob.setContent(job.getContent());
        }

        jobDao.update(job);

        return job;
    }

    /**
     * 启动定时任务
     * @return
     */
    @Transactional
    public boolean startCronFlow(int flowId) throws Exception {
        // quartz 调度
        Flow flow = flowDao.findById(flowId);
        flow.setFlowStatus(1);
        flow.setStartTime(new Date());
        flowDao.update(flow);

        return jobScheduler.startJob(String.valueOf(flow.getId()), flow.getCron());
    }

    /**
     * 启动定时任务
     * @return
     */
    @Transactional
    public boolean stopCronFlow(int flowId) throws Exception {
        // quartz 调度
        Flow flow = flowDao.findById(flowId);
        flow.setFlowStatus(0);
        flowDao.update(flow);

        jobScheduler.stopJob(String.valueOf(flow.getId()));
        return false;
    }

    /**
     * 执行工作流
     * @param flowId
     */
    public void triggerFlow(int flowId, String params) {
        jobTrigger.triggerFlow(flowId, params);
    }

    public List<Job> findAllJobs() {
        return jobDao.select();
    }

    public List<Flow> findAllFlows() {
        return flowDao.select();
    }

    public List<FlowTask> findAllFlowTask() {
        return flowTaskDao.select();
    }

    public List<FlowTask> findByFlowId(int flowId) {
        return  flowTaskDao.findByFlowId(flowId);
    }

    public List<Task> findTaskByFlowTaskId(int flowTaskId) {
        return taskDao.findByFlowTaskId(flowTaskId);
    }

    public Flow getFlowById(Integer flowId) {
        return flowDao.findById(flowId);
    }

}
