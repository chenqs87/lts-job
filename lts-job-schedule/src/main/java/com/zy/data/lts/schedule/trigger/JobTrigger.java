package com.zy.data.lts.schedule.trigger;

import com.google.gson.Gson;
import com.zy.data.lts.core.JobShardType;
import com.zy.data.lts.core.TriggerMode;
import com.zy.data.lts.core.api.ExecutorApi;
import com.zy.data.lts.core.dao.FlowDao;
import com.zy.data.lts.core.dao.FlowTaskDao;
import com.zy.data.lts.core.dao.JobDao;
import com.zy.data.lts.core.dao.TaskDao;
import com.zy.data.lts.core.entity.Flow;
import com.zy.data.lts.core.entity.FlowTask;
import com.zy.data.lts.core.entity.Job;
import com.zy.data.lts.core.entity.Task;
import com.zy.data.lts.core.model.ExecuteRequest;
import com.zy.data.lts.core.model.UpdateTaskHostEvent;
import com.zy.data.lts.schedule.model.Tuple;
import com.zy.data.lts.core.tool.SpringContext;
import com.zy.data.lts.schedule.state.flow.FlowEvent;
import com.zy.data.lts.schedule.state.flow.FlowEventType;
import com.zy.data.lts.schedule.state.flow.FlowTaskStatus;
import com.zy.data.lts.schedule.state.flow.MemFlowTask;
import com.zy.data.lts.schedule.state.task.MemTask;
import com.zy.data.lts.schedule.state.task.TaskStatus;
import com.zy.data.lts.schedule.tools.IntegerTool;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author chenqingsong
 * @date 2019/3/28 12:31
 */
@Component
public class JobTrigger {

    @Autowired
    private FlowDao flowDao;

    @Autowired
    private FlowTaskDao flowTaskDao;

    @Autowired
    private TaskDao taskDao;

    @Autowired
    private JobDao jobDao;

    @Autowired
    private SpringContext springContext;

    @Autowired
    private ExecutorApi executorApi;

    private final BlockingQueue<Integer> flowQueue = new LinkedBlockingQueue<>();

    private final Map<Integer, MemFlowTask> runningFlowTasks = new ConcurrentHashMap<>();

    private final AtomicBoolean isRunning = new AtomicBoolean(true);

    private final CountDownLatch countDownLatch = new CountDownLatch(1);

   /* private final Thread flowTaskThread = new Thread(() -> {
        while (isRunning.get()) {
            try {
                Integer flowId = flowQueue.poll(5, TimeUnit.SECONDS);
                if(flowId != null) {
                    triggerFlow(flowId);
                }
            } catch (InterruptedException ignore) {}
        }
        countDownLatch.countDown();
    });*/

    @PostConstruct
    public void init() {
        // load all un finished flowTasks
       // flowTaskThread.start();

        //判断是否是主节点，主节点加载
        loadUnFinishedFlowTasks();
    }

    @PreDestroy
    public void destroy() {
        isRunning.set(false);
       /* try {
            flowTaskThread.interrupt();
            countDownLatch.await();
        } catch (InterruptedException ignore) {}*/
    }


    @Transactional
    public void triggerFlow(int flowId, TriggerMode triggerMode) {
        triggerFlow(flowId, triggerMode,null);
    }

    @Transactional
    public void triggerFlow(int flowId, TriggerMode triggerMode, String params) {
        Flow flow = flowDao.findById(flowId);
        String config = flow.getFlowConfig();

        FlowTask flowTask = createFlowTask(flow, triggerMode, params);

        List<MemTask> tasks = createAndGetTasks(config, flowTask, flowId);

        MemFlowTask memFlowTask = new MemFlowTask(flowTask, tasks, springContext);
        runningFlowTasks.putIfAbsent(flowTask.getId(), memFlowTask);

        handleFlowTask(new FlowEvent(flowTask.getId(), FlowEventType.Submit));
    }


    /**
     * 只有主节点才会触发
     */
    @Transactional
    public void loadUnFinishedFlowTasks() {
        List<FlowTask> flowTasks = flowTaskDao.findUnFinishedFlowTasks();
        if(CollectionUtils.isEmpty(flowTasks)) {
            return;
        }

        flowTasks.forEach(ft -> {
            FlowTaskStatus status = FlowTaskStatus.parse(ft.getStatus());
            switch (status) {
                case New: handleFlowTask(new FlowEvent(ft.getId(), FlowEventType.Submit)); break;
                case Pending: handleFlowTask(new FlowEvent(ft.getId(), FlowEventType.Execute)); break;
                case Running: handleFlowTask(new FlowEvent(ft.getId(), FlowEventType.Finish)); break;
                default:
            }
        });
    }

    @Transactional
    public void handleFlowTask(FlowEvent flowEvent) {
        MemFlowTask flowTask = getMemFlowTask(flowEvent.getFlowTaskId());
        flowTask.handle(flowEvent);
    }

    public void killFlowTask(int flowTaskId) {
        MemFlowTask flowTask = runningFlowTasks.get(flowTaskId);
        if(flowTask != null) {
            flowTask.handle(new FlowEvent(FlowEventType.Kill));
        }
    }

    private MemFlowTask getMemFlowTask(int flowTaskId) {
        return runningFlowTasks.computeIfAbsent(flowTaskId, f -> {
            FlowTask ft = flowTaskDao.findById(flowTaskId);
            List<Task> tasks = taskDao.findByFlowTaskId(flowTaskId);
            List<MemTask> memTasks = tasks.stream().map(t -> new MemTask(t, springContext)).collect(Collectors.toList());
            return new MemFlowTask(ft, memTasks, springContext);
        });
    }


    public void finishFlowTask(int flowTaskId) {
        runningFlowTasks.remove(flowTaskId);
    }

    /**
     * 发送作业到executor
     * @param task 任务发送队列
     */
    public void sendTask(MemTask task) {
        if(task!= null) {
            Task t = task.getTask();
            List<Integer> shards = IntegerTool.parseOneBit(t.getShardStatus());
            for (Integer shard : shards) {
                executorApi.execute(new ExecuteRequest(t.getFlowTaskId(), t.getTaskId(), shard, t.getHandler()));
            }
        }
    }

    private FlowTask createFlowTask(Flow flow, TriggerMode triggerMode, String params) {
        FlowTask flowTask = new FlowTask();
        flowTask.setFlowId(flow.getId());
        flowTask.setStatus(FlowTaskStatus.New.code());
        flowTask.setBeginTime(new Date());
        flowTask.setParams(StringUtils.isBlank(params) ? flow.getParams() : params);
        flowTask.setTriggerMode(triggerMode.getCode());

        flowTaskDao.insert(flowTask);
        int id = flowTaskDao.getId();
        flowTask.setId(id);
        return flowTask;
    }


    /**
     * 根据 flow 生成task 任务信息
     * @param config  a:b\na:c\nb:d\nc:d
     * @param flowTask flow task
     * @param flowId flow id  从0开始
     * @return
     */
    private List<MemTask> createAndGetTasks(String config, FlowTask flowTask, int flowId) {
        BufferedReader bis =  new BufferedReader(new InputStreamReader(new ByteArrayInputStream(config.getBytes())));

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
            if(t.second() > 0) {
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
            if(second != null) {
                first.setUpPostTask(second.getTaskId());
                second.setUpPreTask(first.getTaskId());
            }
        });

        taskMap.values().forEach(task -> taskDao.insert(task));

        return taskMap.values().stream()
                .map(t -> new MemTask(t, springContext))
                .collect(Collectors.toList());

    }

    private static final Gson gson = new Gson();

    /**
     * params
     * @param jobConfig
     * {
     *     shardCount: 5
     * }
     * @return
     */
    public int getShardStatus(String jobConfig) {
        ShardTask map = gson.fromJson(jobConfig, ShardTask.class);
        Integer shardCount = map.getShardCount();
        return shardCount == null ? 1 : shardCount;
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

    @EventListener
    public void updateTaskHost(UpdateTaskHostEvent event) {
        Task task = taskDao.findById(event.getFlowTaskId(), event.getTaskId());
        task.setHost(event.getHost());
        taskDao.update(task);
    }
}
