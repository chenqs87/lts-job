package com.zy.data.lts.schedule.trigger;

import com.google.gson.Gson;
import com.zy.data.lts.core.JobShardType;
import com.zy.data.lts.core.TriggerMode;
import com.zy.data.lts.core.config.ThreadPoolsConfig;
import com.zy.data.lts.core.dao.FlowDao;
import com.zy.data.lts.core.dao.FlowTaskDao;
import com.zy.data.lts.core.dao.JobDao;
import com.zy.data.lts.core.dao.TaskDao;
import com.zy.data.lts.core.entity.Flow;
import com.zy.data.lts.core.entity.FlowTask;
import com.zy.data.lts.core.entity.Job;
import com.zy.data.lts.core.entity.Task;
import com.zy.data.lts.core.model.ExecuteRequest;
import com.zy.data.lts.core.model.KillTaskRequest;
import com.zy.data.lts.core.model.UpdateTaskHostEvent;
import com.zy.data.lts.core.tool.SpringContext;
import com.zy.data.lts.schedule.handler.HandlerService;
import com.zy.data.lts.schedule.model.Tuple;
import com.zy.data.lts.schedule.state.flow.FlowEvent;
import com.zy.data.lts.schedule.state.flow.FlowEventType;
import com.zy.data.lts.schedule.state.flow.FlowTaskStatus;
import com.zy.data.lts.schedule.state.flow.MemFlowTask;
import com.zy.data.lts.schedule.state.task.MemTask;
import com.zy.data.lts.schedule.state.task.TaskEvent;
import com.zy.data.lts.schedule.state.task.TaskEventType;
import com.zy.data.lts.schedule.state.task.TaskStatus;
import com.zy.data.lts.schedule.tools.IntegerTool;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

/**
 * @author chenqingsong
 * @date 2019/3/28 12:31
 */
@Component
@DependsOn("springContext")
public class JobTrigger {
    private static final Logger logger = LoggerFactory.getLogger(JobTrigger.class);

    @Autowired
    private Gson gson;

    private final Map<Integer, MemFlowTask> runningFlowTasks = new ConcurrentHashMap<>();

    @Autowired
    private FlowDao flowDao;

    @Autowired
    private FlowTaskDao flowTaskDao;

    @Autowired
    private TaskDao taskDao;

    @Autowired
    private JobDao jobDao;

    @Autowired
    private HandlerService handlerService;

    public static void triggerCronFlow(Integer flowId) {
        JobTrigger jobTrigger = SpringContext.getBean(JobTrigger.class);
        jobTrigger.triggerFlow(flowId, TriggerMode.Cron);
    }

    @PostConstruct
    public void init() {
        loadUnFinishedFlowTasks();
    }

    @PreDestroy
    public void destroy() {
    }

    public void triggerFlow(int flowId, TriggerMode triggerMode) {
        JobTrigger jobTrigger = SpringContext.getBean(JobTrigger.class);
        FlowTask flowTask = jobTrigger.getFlowTask(flowId, triggerMode, null);
        jobTrigger.handleFlowTask(new FlowEvent(flowTask.getId(), FlowEventType.Submit));
    }

    /**
     * 该方法提交完毕，必须提交事务，否则在后续任务发送成功后，
     * 可能由于当前事务未提交造成查不到task任务信息
     * @param flowId
     * @param triggerMode
     * @param params
     * @return
     */
    @Transactional(propagation = REQUIRES_NEW)
    public FlowTask getFlowTask(int flowId, TriggerMode triggerMode, String params) {
        Flow flow = flowDao.findById(flowId);
        String config = flow.getFlowConfig();

        FlowTask flowTask = createFlowTask(flow, triggerMode, params);

        List<MemTask> tasks = createAndGetTasks(config, flowTask, flowId);

        MemFlowTask memFlowTask = new MemFlowTask(flowTask, tasks);
        runningFlowTasks.putIfAbsent(flowTask.getId(), memFlowTask);
        return flowTask;
    }

    /**
     * 根据失败的工作流，构建新的工作流，在原有工作流中已经完成的任务，直接跳过
     *
     * @param flowTaskId 工作流任务ID
     * @param params     工作流任务参数
     */
    @Transactional(propagation = REQUIRES_NEW)
    public FlowTask buildFlowTaskForFailed(int flowTaskId, String params) {
        FlowTask flowTask = flowTaskDao.findById(flowTaskId);
        Flow flow = flowDao.findById(flowTask.getFlowId());

        List<Task> tasks = taskDao.findByFlowTaskId(flowTaskId);

        Set<Integer> successJobIds = new HashSet<>();
        tasks.forEach(t -> {
            if (TaskStatus.parse(t.getTaskStatus()) == TaskStatus.Finished) {
                successJobIds.add(t.getJobId());
            }
        });

        FlowTask newFlowTask = createFlowTask(flow, TriggerMode.Click, params);

        String config = flow.getFlowConfig();
        List<MemTask> newTasks = createAndGetTasks(config, newFlowTask, flow.getId());

        MemFlowTask memFlowTask = new MemFlowTask(newFlowTask, newTasks);
        runningFlowTasks.putIfAbsent(newFlowTask.getId(), memFlowTask);

        newTasks.forEach(mt -> {
            Task t = mt.getTask();
            if (successJobIds.contains(t.getJobId())) {
                mt.setSkip(true);
            }
        });
        return newFlowTask;
    }


    public void loadUnFinishedFlowTasks() {
        List<FlowTask> flowTasks = flowTaskDao.findUnFinishedFlowTasks();
        if (CollectionUtils.isEmpty(flowTasks)) {
            return;
        }

        flowTasks.forEach(ft -> {
            try {
                FlowTaskStatus status = FlowTaskStatus.parse(ft.getStatus());
                switch (status) {
                    case New:
                        handleFlowTask(new FlowEvent(ft.getId(), FlowEventType.Submit));
                        break;
                    case Pending:
                        handleFlowTask(new FlowEvent(ft.getId(), FlowEventType.Execute));
                        break;
                    case Running:
                        handleFlowTask(new FlowEvent(ft.getId(), FlowEventType.Finish));
                        break;
                    default:
                }
            }catch (Exception e) {
                // TODO 程序启动的时候，如果有作业，不能正常加载触发，需要记录并解决改问题。
                logger.error("Failed to load FlowTask [{}]", ft.getId(), e);
            }

        });
    }

    @Transactional(propagation = REQUIRES_NEW)
    @Async(ThreadPoolsConfig.SUBMIT_FLOW_TASK_THREAD_POOL)
    public void handleFlowTask(FlowEvent flowEvent) {
        MemFlowTask flowTask = getMemFlowTask(flowEvent.getFlowTaskId());
        flowTask.handle(flowEvent);
    }

    public void killFlowTask(int flowTaskId) {
        handleFlowTask(new FlowEvent(flowTaskId, FlowEventType.Kill));
    }

    private MemFlowTask getMemFlowTask(int flowTaskId) {
        return runningFlowTasks.computeIfAbsent(flowTaskId, f -> {
            FlowTask ft = flowTaskDao.findById(flowTaskId);
            List<Task> tasks = taskDao.findByFlowTaskId(flowTaskId);
            List<MemTask> memTasks = tasks.stream().map(MemTask::new).collect(Collectors.toList());
            return new MemFlowTask(ft, memTasks);
        });
    }

    public void finishFlowTask(int flowTaskId) {
        runningFlowTasks.remove(flowTaskId);
    }

    /**
     * 发送作业到executor
     *
     * @param task 任务发送队列
     */
    @Transactional(propagation = REQUIRES_NEW)
    @Async(ThreadPoolsConfig.SUBMIT_FLOW_TASK_THREAD_POOL)
    public void sendTask(MemTask task) {
        if (task != null) {
            Task t = task.getTask();

            List<Integer> shards = IntegerTool.parseOneBit(t.getShardStatus());
            for (Integer shard : shards) {
                if (!task.isSkip()) {
                    handlerService.execute(new ExecuteRequest(t.getFlowTaskId(), t.getTaskId(), shard, t.getHandler()));
                    // 发送成功,则认为任务开始执行
                    handleFlowTask(new FlowEvent(t.getFlowTaskId(), FlowEventType.Execute, t.getTaskId(), shard));
                } else {
                    handleFlowTask(new FlowEvent(t.getFlowTaskId(), FlowEventType.Execute, t.getTaskId(), shard));
                    handleFlowTask(new FlowEvent(t.getFlowTaskId(), FlowEventType.Finish, t.getTaskId(), shard));
                }
            }
        }
    }

    /**
     * 创建执行的工作流
     * @param flow 对用流
     * @param triggerMode 触发种类
     * @param params 工作参数
     * @return
     */
    private FlowTask createFlowTask(Flow flow, TriggerMode triggerMode, String params) {
        FlowTask flowTask = new FlowTask();
        flowTask.setFlowId(flow.getId());
        flowTask.setStatus(FlowTaskStatus.New.code());
        flowTask.setBeginTime(new Date());
        flowTask.setParams(StringUtils.isBlank(params) ? flow.getParams() : params);
        flowTask.setTriggerMode(triggerMode.getCode());

        flowTaskDao.insert(flowTask);
        return flowTask;
    }

    /**
     * 根据 flow 生成task 任务信息
     * 为了便于存储解析和管理，所有task的 taskId 从0开始计算
     * 最大为31,所以一个工作流的最大任务数是32，如果任务数超过32个，建议拆成两个父子工作流来调度
     *
     * @param config   a:b\na:c\nb:d\nc:d
     * @param flowTask flow task
     * @param flowId   flow id  从0开始
     * @return
     */
    private List<MemTask> createAndGetTasks(String config, FlowTask flowTask, int flowId) {
        BufferedReader bis = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(config.getBytes())));

        List<Tuple<Integer, Integer>> list = bis.lines()
                .map(l -> l.split(":"))
                .filter(ArrayUtils::isNotEmpty)
                .map(s -> new Tuple<>(Integer.parseInt(s[0]),
                        s.length == 2 ? Integer.parseInt(s[1]) : -1))
                .collect(Collectors.toList());

        // <jobId, taskId>
        Map<Integer, Integer> map = new HashMap<>();
        list.forEach(t -> {
            map.putIfAbsent(t.first(), map.size());
            if (t.second() > 0) {
                map.putIfAbsent(t.second(), map.size());
            }
        });

        Map<Integer, Task> taskMap = new HashMap<>();
        map.forEach((jobId, taskId) -> {
            Task task = new Task();
            task.setJobId(jobId);
            task.setTaskId(taskId);
            task.setFlowTaskId(flowTask.getId());
            task.setBeginTime(new Date());
            task.setFlowId(flowId);
            task.setTaskStatus(TaskStatus.New.code());

            Job job = jobDao.findById(jobId);
            //TODO :: IntegerTool.format(0, shardCount)
            int shardStatus = job.getShardType() == JobShardType.NONE.code() ?
                    1 : IntegerTool.format(0, getShardStatus(job.getConfig()));
            task.setShardStatus(shardStatus);

            task.setHandler(job.getHandler());
            taskMap.put(jobId, task);
        });

        //设置前置和后置任务
        list.forEach(t -> {
            Task first = taskMap.get(t.first());
            Task second = taskMap.get(t.second());
            if (second != null) {
                first.setUpPostTask(second.getTaskId());
                second.setUpPreTask(first.getTaskId());
            }
        });

        taskMap.values().forEach(task -> taskDao.insert(task));

        return taskMap.values().stream()
                .map(MemTask::new)
                .collect(Collectors.toList());

    }

    /**
     * params
     *
     * @param jobConfig {shardCount: 5}
     * @return
     */
    public int getShardStatus(String jobConfig) {
        ShardTask map = gson.fromJson(jobConfig, ShardTask.class);
        Integer shardCount = map.getShardCount();
        return shardCount == null ? 1 : shardCount;
    }

    public void handleUnFinishFlow(MemFlowTask memFlowTask) {
        //程序启动触发，为完成的工作流，当前flow 状态为Running，
        //flow中的task可能存在各种状态，例如：Ready Pending Running 等等
        memFlowTask.getTasks()
                .forEach(t -> {
                    TaskStatus status = TaskStatus.parse(t.getTask().getTaskStatus());
                    switch (status) {
                        case New:
                            t.handle(new TaskEvent(TaskEventType.Submit));
                            break;
                        case Ready:
                            t.handle(new TaskEvent(TaskEventType.Pend));
                            break;
                        case Submitted:
                            t.handle(new TaskEvent(TaskEventType.Execute));
                            break;
                        case Pending:
                            t.handle(new TaskEvent(TaskEventType.Send));
                            break;
                        case Running:
                            break; // 作业在executor 中运行，极端情况，executor 挂了，可能会造成整个工作流不可用
                        case Failed:
                        case Killed:
                        case Finished:
                            throw new IllegalStateException("Fail to execute flow " + memFlowTask.getFlowTask().getId());
                    }
                });
    }

    public void killTask(KillTaskRequest request) {
        handlerService.kill(request);
    }

    @EventListener
    public void updateTaskHost(UpdateTaskHostEvent event) {
        MemFlowTask flowTask = runningFlowTasks.get(event.getFlowTaskId());
        if (flowTask != null) {
            MemTask memTask = flowTask.getMemTask(event.getTaskId());
            if (memTask != null) {
                memTask.lock();
                try {
                    Task task = memTask.getTask();
                    task.setHost(event.getHost());
                    taskDao.update(task);
                } finally {
                    memTask.unlock();
                }
            }
        }
    }

    public static class ShardTask {
        private Integer shardCount;

        public Integer getShardCount() {
            return shardCount;
        }

        public void setShardCount(Integer shardCount) {
            this.shardCount = shardCount;
        }
    }
}
