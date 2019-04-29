package com.zy.data.lts.schedule.state.flow.transition;

import com.zy.data.lts.core.dao.FlowTaskDao;
import com.zy.data.lts.core.dao.TaskDao;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class FlowFinishTransition implements MultipleArcTransition<MemFlowTask, FlowEvent, FlowTaskStatus> {

    @Autowired
    FlowTaskDao flowTaskDao;

    @Autowired
    TaskDao taskDao;

    @Autowired
    JobTrigger jobTrigger;

    @Override
    public FlowTaskStatus transition(MemFlowTask memFlowTask, FlowEvent flowEvent) {
        memFlowTask.lock();

        try {
            int taskId = flowEvent.getCurrentTaskId();
            if(taskId == -1) {
                //程序启动触发，为完成的工作流
                memFlowTask.getTasks().forEach(t -> t.handle(new TaskEvent(TaskEventType.Submit)));
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
}