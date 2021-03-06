package com.zy.data.lts.schedule.service;

import com.cronutils.mapper.CronMapper;
import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronConstraintsFactory;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import com.github.pagehelper.PageHelper;
import com.google.gson.Gson;
import com.zy.data.lts.core.*;
import com.zy.data.lts.core.dao.*;
import com.zy.data.lts.core.entity.*;
import com.zy.data.lts.core.model.ExecLogEvent;
import com.zy.data.lts.core.model.FlowQueryRequest;
import com.zy.data.lts.core.model.JobQueryRequest;
import com.zy.data.lts.schedule.state.flow.FlowEvent;
import com.zy.data.lts.schedule.state.flow.FlowEventType;
import com.zy.data.lts.schedule.timer.JobScheduler;
import com.zy.data.lts.schedule.trigger.JobTrigger;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.cronutils.model.CronType.QUARTZ;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

/**
 * @author chenqingsong
 * @date 2019/3/29 11:56
 */
@Service
public class JobService {

    private static final Logger logger = LoggerFactory.getLogger(JobService.class);

    @Autowired
    JobTrigger jobTrigger;

    @Autowired
    JobScheduler jobScheduler;

    @Autowired
    UserDao userDao;

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

    @Autowired
    FlowScheduleLogDao flowScheduleLogDao;

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

        //将对应的工作流查看权限添加至用户的对应组
        RepmPolicy group = new RepmPolicy();
        group.setCreateTime(new Date());
        group.setPermit(LtsPermitEnum.FlowView.code);
        group.setType(LtsPermitType.Flow.name());
        group.setResource(flow.getId());
        group.setPolicyName(repmPolicyDao.wrapGroup(userDao.findByName(flow.getCreateUser()).getGroupName()));
        repmPolicyDao.insert(group);


        flow.setFlowId(flow.getId());
        alertConfigDao.insert(flow);
        return flow;
    }


    /**
     *
     * @param userName
     * @param importDataFlow
     * @return
     */
    @Transactional
    public Flow createImportDataFlow(String userName, ImportDataFlow importDataFlow) {
        //校验数据大小
        Job checkSizeJob = new Job();
        checkSizeJob.setName(importDataFlow.getGroup() + "ChkSize");
        checkSizeJob.setHandler(importDataFlow.getSizeHandler());
        checkSizeJob.setJobType("shell");
        checkSizeJob.setCreateTime(new Date());
        checkSizeJob.setCreateUser(userName);
        checkSizeJob.setContent(importDataFlow.getSize());
        checkSizeJob.setShardType(0);
        checkSizeJob.setConfig("");
        checkSizeJob.setGroup(importDataFlow.getGroup());
        jobDao.insert(checkSizeJob);

        RepmPolicy checkSizeJobRep = new RepmPolicy();
        checkSizeJobRep.setCreateTime(new Date());
        checkSizeJobRep.setPolicyName(repmPolicyDao.wrapUsername(userName));
        checkSizeJobRep.setResource(checkSizeJob.getId());
        checkSizeJobRep.setPermit(LtsPermitEnum.getAllJobPermit());
        checkSizeJobRep.setType(LtsPermitType.Job.name());
        repmPolicyDao.insert(checkSizeJobRep);


        //校验数据内容
        Job checkContentJob = new Job();
        checkContentJob.setName(importDataFlow.getGroup() + "ChkFile");
        checkContentJob.setHandler(importDataFlow.getContentHandler());
        checkContentJob.setJobType("ChkFile");
        checkContentJob.setCreateTime(new Date());
        checkContentJob.setCreateUser(userName);
        checkContentJob.setContent(importDataFlow.getContent());
        checkContentJob.setShardType(0);
        checkContentJob.setConfig("");
        checkContentJob.setGroup(importDataFlow.getGroup());
        jobDao.insert(checkContentJob);

        RepmPolicy checkContentJobRep = new RepmPolicy();
        checkContentJobRep.setCreateTime(new Date());
        checkContentJobRep.setPolicyName(repmPolicyDao.wrapUsername(userName));
        checkContentJobRep.setResource(checkContentJob.getId());
        checkContentJobRep.setPermit(LtsPermitEnum.getAllJobPermit());
        checkContentJobRep.setType(LtsPermitType.Job.name());
        repmPolicyDao.insert(checkContentJobRep);

        Job importDataJob = jobDao.selectImportDataJob();
        if(importDataJob == null) {
            throw new IllegalArgumentException("The job [ImportCommonJob] is not exist !");
        }

        Flow flow = new Flow();
        flow.setCreateUser(userName);
        flow.setName(importDataFlow.getGroup() + "ImportData");
        flow.setCron(importDataFlow.getCron());
        flow.setCreateTime(new Date());
        flow.setFlowConfig(checkSizeJob.getId() + ":" + checkContentJob.getId() + "\n"
                + checkContentJob.getId() + ":" + importDataJob.getId());
        flow.setParams(importDataFlow.getConfig());
        flow.setFlowEditorInfo(createEditorInfo(checkSizeJob.getId(), checkContentJob.getId(), importDataJob.getId()));
        flowDao.insert(flow);
        flowDao.update(flow);

        RepmPolicy flowRep = new RepmPolicy();
        flowRep.setCreateTime(new Date());
        flowRep.setPolicyName(repmPolicyDao.wrapUsername(flow.getCreateUser()));
        flowRep.setResource(flow.getId());
        flowRep.setPermit(LtsPermitEnum.getAllFlowPermit());
        flowRep.setType(LtsPermitType.Flow.name());
        repmPolicyDao.insert(flowRep);

        //将对应的工作流查看权限添加至用户的对应组
        RepmPolicy group = new RepmPolicy();
        group.setCreateTime(new Date());
        group.setPermit(LtsPermitEnum.FlowView.code);
        group.setType(LtsPermitType.Flow.name());
        group.setResource(flow.getId());
        group.setPolicyName(
                repmPolicyDao.wrapGroup(userDao.findByName(flow.getCreateUser()).getGroupName()));
        repmPolicyDao.insert(group);

        return flow;
    }

    /**
     * todo 优化
     * @param id1
     * @param id2
     * @param id3
     * @return
     */
    private String createEditorInfo(int id1, int id2, int id3) {
        try(InputStream is =
                    JobService.class.getClassLoader().getResourceAsStream("ImportDataEditorModel.json");
            Reader reader = new InputStreamReader(is)) {
            Gson gson = new Gson();
            Map map = gson.fromJson(reader, Map.class);
            List nodes = (List) map.get("nodes");
            nodes.forEach(node -> {
                Map n = (Map)node;
                String id= (String)n.get("id");

                switch (id) {
                    case "fce0c4e9": n.put("shape", String.valueOf(id1));break;
                    case "2dc7de71": n.put("shape", String.valueOf(id2));break;
                    case "dd1d3767": n.put("shape", String.valueOf(id3));break;
                }
            });
            return gson.toJson(map);
        } catch (IOException e) {
            throw new IllegalArgumentException("Fail to create flow editor info!", e);
        }

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

        flow.setFlowId(flow.getId());
        AlertConfig alertConfig = alertConfigDao.findByFlowId(flow.getFlowId());
        if(alertConfig == null) {
            alertConfigDao.insert(flow);
        } else {
            alertConfigDao.update(flow);
        }

        return flow;
    }

    @Transactional
    public Job createJob(Job job) {
        job.setCreateTime(new Date());
        jobDao.insert(job);

        // 用户对应的用户组添加对应任务权限
        RepmPolicy group = new RepmPolicy();
        group.setResource(job.getId());
        group.setCreateTime(new Date());
        group.setPolicyName(repmPolicyDao.wrapGroup(userDao.findByName(job.getCreateUser()).getGroupName()));
        group.setType(LtsPermitType.Job.name());
        group.setPermit(LtsPermitEnum.JobView.code);
        repmPolicyDao.insert(group);

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

        Date date = jobScheduler.startJob(String.valueOf(flow.getId()), parseCron(flow.getCron()));
        if(date != null) {
            flow.setIsSchedule(1);
            flow.setStartTime(date);
            flowDao.update(flow);
        }
        return flow;
    }

    private static CronDefinition quartz() {
        return CronDefinitionBuilder.defineCron()
                .withSeconds().withStrictRange().and()
                .withMinutes().withStrictRange().and()
                .withHours().withStrictRange().and()
                .withDayOfMonth().withValidRange(1, 32).supportsL().supportsW().supportsLW().supportsQuestionMark().withStrictRange().and()
                .withMonth().withValidRange(1, 13).and()
                .withDayOfWeek().withValidRange(0, 6).withMondayDoWValue(1).supportsHash().supportsL().supportsQuestionMark().and()
                .withYear().withValidRange(1970, 2099).withStrictRange().optional().and()
                .withCronValidation(CronConstraintsFactory.ensureEitherDayOfWeekOrDayOfMonth())
                .instance();
    }

    /**
     * cron 表达式转换，通过vue控件生成的cron 表达式与quartz表达式，有些细微的差别，
     * 主要体现在 vue控件生成的cron表达式，周的表示范围是（0~7），其中0和7都表示周日
     * 而quartz 调度的cron表达式， 周的表示范围时（1~7）, 1 表示周日， 再通过quartz
     * 因此，quartz在进行调度的时候，需要对cron表达式进行转换。
     * @param unixCron
     * @return
     */
    private String parseCron(String unixCron) {

        CronDefinition cronDefinition = quartz();
        CronParser parser = new CronParser(cronDefinition);
        Cron from = parser.parse(unixCron);

        CronMapper cronMapper = new CronMapper(cronDefinition,
                CronDefinitionBuilder.instanceDefinitionFor(QUARTZ), cron -> cron);
        Cron quartzCron = cronMapper.map(from);
        return quartzCron.asString();
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

        FlowTask flowTask = jobTrigger.getFlowTask(flowId, triggerMode, params);
        jobTrigger.handleFlowTask(new FlowEvent(flowTask.getId(), FlowEventType.Submit));
    }

    /**
     * 工作流失败后，重新触发工作流
     *
     * @param flowTaskId
     */
    @Transactional
    public void reTriggerFlow(int flowTaskId, String params) {
        FlowTask flowTask = jobTrigger.buildFlowTaskForFailed(flowTaskId, params);
        jobTrigger.handleFlowTask(new FlowEvent(flowTask.getId(), FlowEventType.Submit));

    }

    public List<Job> findAllJobs(JobQueryRequest request) {
        PageHelper.startPage(request.getPageNum(), request.getPageSize());
        List<Job> ret = jobDao.select(request);

        int permit = request.getRole() == RoleEnum.ROLE_ADMIN ? LtsPermitEnum.getAllJobPermit() :
                LtsPermitEnum.JobView.code;

        if (CollectionUtils.isNotEmpty(ret)) {
            ret.forEach(j -> j.setPermit(permit));
        }

        return ret;
    }

    public List<Job> findJobsByUser(JobQueryRequest request) {
        PageHelper.startPage(request.getPageNum(), request.getPageSize());
        return jobDao.selectByUser(request);
    }

    public List<Job> findJobsByGroup(JobQueryRequest request) {
        User user = userDao.findByName(request.getUsername());
        request.setUserGroup(user.getGroupName());

        PageHelper.startPage(request.getPageNum(), request.getPageSize());
        return jobDao.selectByGroup(request);
    }

    public List<Flow> findAllFlows(FlowQueryRequest request) {

        PageHelper.startPage(request.getPageNum(), request.getPageSize());
        List<Flow> ret = flowDao.select();

        int permit = request.getRole() == RoleEnum.ROLE_ADMIN ? LtsPermitEnum.getAllFlowPermit() :
                LtsPermitEnum.FlowView.code;
        if (CollectionUtils.isNotEmpty(ret)) {
            ret.forEach(j -> j.setPermit(permit));
        }

        return ret;
    }

    public List<Flow> findFlowsByUser(FlowQueryRequest request) {
        PageHelper.startPage(request.getPageNum(), request.getPageSize());
        return flowDao.selectByUser(request.getUsername(), request.getPermit());
    }

    public List<Flow> findFlowsByGroup(FlowQueryRequest request) {
        User user = userDao.findByName(request.getUsername());
        PageHelper.startPage(request.getPageNum(), request.getPageSize());
        return flowDao.selectByGroup(user.getGroupName(), request.getPermit());
    }

    public List<FlowTask> findFlowTask(int flowId, int statusId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return flowTaskDao.select(flowId, statusId);
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

    public List<FlowScheduleLog> getFlowScheduleLog(int flowTaskId) {
        return flowScheduleLogDao.select(flowTaskId);
    }

    @Transactional
    @EventListener
    public void triggerFlow(TriggerFlowEvent event) {
        triggerFlow(event.getFlowId(), event.getTriggerMode(), event.getParams());

        if(event.getTriggerMode() == TriggerMode.Cron) {
            try {
                Flow dbFlow = flowDao.findById(event.getFlowId());
                CronExpression cronExpression = new CronExpression(parseCron(dbFlow.getCron()));
                Date date = cronExpression.getNextValidTimeAfter(new Date());
                dbFlow.setStartTime(date);
                flowDao.update(dbFlow);

            } catch (Exception e) {
                logger.warn("Fail to update the flow [{}] next trigger time!!!", event.getFlowId(), e);
            }
        }
    }

    @EventListener
    @Transactional(propagation = REQUIRES_NEW)
    public void writeExecLog(ExecLogEvent event) {
        jobTrigger.doWriteLog(event.getFlowTaskId(), event.getMsg());
    }

    public void handleFlowTask(FlowEvent flowEvent) {
        jobTrigger.handleFlowTask(flowEvent);
    }

    public int getRunningTaskSize() {
        return flowTaskDao.findRunningFlowTasks();
    }

    public List<Map<String, Object>> countTasksByDay() {
        Calendar from = Calendar.getInstance();
        from.add(Calendar.DATE, -7);
        from.set(Calendar.HOUR_OF_DAY, 0);
        from.set(Calendar.MINUTE, 0);
        from.set(Calendar.SECOND, 0);

        Calendar end = Calendar.getInstance();
        end.add(Calendar.DATE, 1);
        end.set(Calendar.HOUR_OF_DAY, 0);
        end.set(Calendar.MINUTE, 0);
        end.set(Calendar.SECOND, 0);

        return flowTaskDao.countTasksByDay(from.getTime(), end.getTime());
    }
}