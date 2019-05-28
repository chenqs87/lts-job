package com.zy.data.lts.schedule.service;

import com.github.pagehelper.PageHelper;
import com.zy.data.lts.core.TriggerMode;
import com.zy.data.lts.core.dao.*;
import com.zy.data.lts.core.entity.*;
import com.zy.data.lts.core.model.JobQueryRequest;
import com.zy.data.lts.schedule.timer.JobScheduler;
import com.zy.data.lts.schedule.trigger.JobTrigger;
import org.apache.commons.lang3.StringUtils;
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

    @Autowired
    RepmPolicyDao repmPolicyDao;

    @Autowired
    AlertConfigDao alertConfigDao;

    /**
     * 创建工作流
     *
     * @param flow
     * @throws Exception
     */
    @Transactional
    public Flow createFlow(AlertConfig flow) {

        flow.setCreateTime(new Date());
        flowDao.insert(flow);

        flow.setPolicyName(repmPolicyDao.wrapUsername(flow.getCreateUser()));
        flow.setResource(flow.getId());
        repmPolicyDao.insert(flow);

        flow.setFlowId(flow.getId());
        alertConfigDao.insert(flow);
        return flow;
    }

    @Transactional
    public void deleteFlow(int flowId) {
        repmPolicyDao.delete("Flow", flowId);
        flowDao.delete(flowId);
    }

    @Transactional
    public Flow updateFlow(AlertConfig flow) {
        Flow dbFlow = flowDao.findById(flow.getId());

        //更新工作流基本信息
        if (StringUtils.isNotBlank(flow.getName())) {
            dbFlow.setName(flow.getName());
            dbFlow.setCron(flow.getCron());
            dbFlow.setParams(flow.getParams());
            dbFlow.setPostFlow(flow.getPostFlow());
        }

        // 更新工作流配置信息
        if (StringUtils.isNotBlank(flow.getFlowEditorInfo())) {
            dbFlow.setFlowConfig(flow.getFlowConfig());
            dbFlow.setFlowEditorInfo(flow.getFlowEditorInfo());
        }

        flowDao.update(dbFlow);


        if (StringUtils.isNotBlank(flow.getPhoneList())
                || StringUtils.isNotBlank(flow.getEmailList())) {
            alertConfigDao.update(flow);
        }
        return flow;
    }

    @Transactional
    public Job createJob(Job job) {
        job.setCreateTime(new Date());
        jobDao.insert(job);

        job.setPolicyName(repmPolicyDao.wrapUsername(job.getCreateUser()));
        job.setResource(job.getId());
        repmPolicyDao.insert(job);
        return job;
    }

    @Transactional
    public void deleteJob(int jobId) {
        repmPolicyDao.delete("Job", jobId);
        jobDao.delete(jobId);
    }

    @Transactional
    public Job updateJob(Job job) {
        Job dbJob = jobDao.findById(job.getId());

        if (StringUtils.isNotBlank(job.getName())) {
            dbJob.setName(job.getName());
            dbJob.setConfig(job.getConfig());
            dbJob.setHandler(job.getHandler());
            dbJob.setJobType(job.getJobType());
            dbJob.setShardType(job.getShardType());
        }

        if (StringUtils.isNotBlank(job.getContent())) {
            dbJob.setContent(job.getContent());
        }

        jobDao.update(job);

        return job;
    }

    /**
     * 启动定时任务
     *
     * @return
     */
    @Transactional
    public Flow startCronFlow(int flowId) throws Exception {
        // quartz 调度
        Flow flow = flowDao.findById(flowId);
        flow.setIsSchedule(1);
        flow.setStartTime(new Date());
        flowDao.update(flow);

        jobScheduler.startJob(String.valueOf(flow.getId()), flow.getCron());
        return flow;
    }

    /**
     * 启动定时任务
     *
     * @return
     */
    @Transactional
    public Flow stopCronFlow(int flowId) throws Exception {
        // quartz 调度
        Flow flow = flowDao.findById(flowId);
        flow.setIsSchedule(0);
        flowDao.update(flow);

        jobScheduler.stopJob(String.valueOf(flow.getId()));
        return flow;
    }

    /**
     * 执行工作流
     *
     * @param flowId
     */
    @Transactional
    public void triggerFlow(int flowId, TriggerMode triggerMode, String params) {
        jobTrigger.triggerFlow(flowId, triggerMode, params);
    }

    /**
     * 执行工作流
     *
     * @param flowTaskId
     */
    @Transactional
    public void reTriggerFlow(int flowTaskId, String params) {
        jobTrigger.reTriggerFlow(flowTaskId, params);
    }

    public List<Job> findAllJobs(JobQueryRequest request) {
        PageHelper.startPage(request.getPageNum(), request.getPageSize());
        return jobDao.select(request);
    }

    public List<Job> findJobsByUser(JobQueryRequest request) {
        PageHelper.startPage(request.getPageNum(), request.getPageSize());
        return jobDao.selectByUser(request);
    }

    public List<Job> findJobsByGroup(JobQueryRequest request) {
        PageHelper.startPage(request.getPageNum(), request.getPageSize());
        return jobDao.selectByUser(request);
    }

    public List<Flow> findAllFlows(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return flowDao.select();
    }

    public List<Flow> findFlowsByUser(Integer pageNum, Integer pageSize,
                                      String userName, int permit) {
        PageHelper.startPage(pageNum, pageSize);
        return flowDao.selectByUser(userName, permit);
    }

    public List<Flow> findFlowsByGroup(Integer pageNum, Integer pageSize,
                                       String groupName, int permit) {
        PageHelper.startPage(pageNum, pageSize);
        return flowDao.selectByGroup(groupName, permit);
    }

    public List<FlowTask> findAllFlowTask(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return flowTaskDao.select();
    }

    public List<FlowTask> findByFlowId(int flowId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return flowTaskDao.findByFlowId(flowId);
    }

    public List<Task> findTaskByFlowTaskId(int flowTaskId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return taskDao.findByFlowTaskId(flowTaskId);
    }

    public Flow getFlowById(Integer flowId) {
        return flowDao.findById(flowId);
    }

    public void killFlowTask(int flowTaskId) {
        jobTrigger.killFlowTask(flowTaskId);
    }

    public AlertConfig getAlertConfig(Integer flowId) {
        return alertConfigDao.findByFlowId(flowId);
    }
}