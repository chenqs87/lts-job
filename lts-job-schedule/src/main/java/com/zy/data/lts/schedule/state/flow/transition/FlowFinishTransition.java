package com.zy.data.lts.schedule.state.flow.transition;

import com.zy.data.lts.core.TriggerMode;
import com.zy.data.lts.core.dao.FlowDao;
import com.zy.data.lts.core.dao.FlowTaskDao;
import com.zy.data.lts.core.dao.TaskDao;
import com.zy.data.lts.core.entity.Flow;
import com.zy.data.lts.core.entity.FlowTask;
import com.zy.data.lts.core.entity.Task;
import com.zy.data.lts.schedule.state.flow.FlowTaskStatus;
import com.zy.data.lts.schedule.state.MultipleArcTransition;
import com.zy.data.lts.schedule.state.flow.FlowEvent;
import com.zy.data.lts.schedule.state.flow.MemFlowTask;
import com.zy.data.lts.schedule.state.task.MemTask;
import com.zy.data.lts.schedule.state.task.TaskEvent;
import com.zy.data.lts.schedule.state.task.TaskEventType;
import com.zy.data.lts.schedule.state.task.TaskStatus;
import com.zy.data.lts.schedule.tools.IntegerTool;
import com.zy.data.lts.schedule.trigger.JobTrigger;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

@Component
public class FlowFinishTransition implements MultipleArcTransition<MemFlowTask, FlowEvent, FlowTaskStatus> {

    @Autowired
    FlowTaskDao flowTaskDao;

    @Autowired
    TaskDao taskDao;

    @Autowired
    JobTrigger jobTrigger;

    @Autowired
    FlowDao flowDao;

    @Override
    public FlowTaskStatus transition(MemFlowTask memFlowTask, FlowEvent flowEvent) {
        memFlowTask.lock();

        try {
            int taskId = flowEvent.getCurrentTaskId();
            if(taskId == -1) {
                handleUnFinishFlow(memFlowTask);
            } else {
                // handler 回调触发
                MemTask memTask = memFlowTask.getMemTask(taskId);
                memTask.handle(new TaskEvent(TaskEventType.Finish, flowEvent.getCurrentTaskShard()));

                //shard作业
                if(memTask.getTask().getTaskStatus() == TaskStatus.Running.code()) {
                    return FlowTaskStatus.Running;
                }

                memFlowTask.finishTask(taskId);

                // 判断是否有后置任务，如果没有，则工作流结束
                Task task = memTask.getTask();
                if(task.getPostTask() == 0 && memFlowTask.isFinished()) {
                    FlowTask ft = memFlowTask.getFlowTask();
                    ft.setStatus(FlowTaskStatus.Finished.code());
                    ft.setEndTime(new Date());
                    flowTaskDao.update(ft);
                    jobTrigger.finishFlowTask(ft.getId());

                    // 触发子工作流
                    checkAndTriggerPostFlowTasks(ft.getFlowId());

                    return FlowTaskStatus.Finished;
                }

                //更新后置任务状态，并触发后置任务执行
                if(task.getPostTask() > 0) {
                    List<Integer> taskIds = IntegerTool.parseOneBit(task.getPostTask());
                    taskIds.forEach(id -> {
                        /*
                         * 正常情况下，这一步不是必须的，我们一般只判断内存对象的状态，
                         * 更新数据库是为了避免由于master切换或者重启造成的状态不一致
                         */

                        MemTask mt = memFlowTask.getMemTask(id);
                        mt.getTask().completePreTask(taskId);
                        taskDao.update(mt.getTask());

                        mt.handle(new TaskEvent(TaskEventType.Pend));

                        if(mt.getCurrentStatus() == TaskStatus.Pending) {
                            mt.handle(new TaskEvent(TaskEventType.Send));
                        }

                    });
                }
            }

            return FlowTaskStatus.Running;
        } finally {
            memFlowTask.unlock();
        }
    }

    private void checkAndTriggerPostFlowTasks(int flowId) {
        try {
            Flow flow = flowDao.findById(flowId);
            if(StringUtils.isNotBlank(flow.getPostFlow())) {
                String[] postFlowIds = flow.getPostFlow().split(",");
                Stream.of(postFlowIds).map(Integer::parseInt).forEach( id -> {
                    jobTrigger.triggerFlow(id, TriggerMode.PreTask);
                    // TODO 完善日志
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            // TODO 完善日志
        }
    }

    private void handleUnFinishFlow(MemFlowTask memFlowTask) {
        //程序启动触发，为完成的工作流，当前flow 状态为Running，
        //flow中的task可能存在各种状态，例如：Ready Pending Running 等等
        memFlowTask.getTasks()
            .forEach(t -> {
                TaskStatus status = TaskStatus.parse(t.getTask().getTaskStatus());
                switch (status) {
                    case New: t.handle(new TaskEvent(TaskEventType.Submit)); break;
                    case Ready: t.handle(new TaskEvent(TaskEventType.Pend)); break;
                    case Submitted: t.handle(new TaskEvent(TaskEventType.Execute)); break;
                    case Pending: t.handle(new TaskEvent(TaskEventType.Send)); break;
                    case Running: break; // 作业在executor 中运行，极端情况，executor 挂了，可能会造成整个工作流不可用
                    case Failed:
                    case Killed:
                    case Finished:
                        throw new IllegalStateException("Fail to execute flow " + memFlowTask.getFlowTask().getId());
                }

            });
    }
}