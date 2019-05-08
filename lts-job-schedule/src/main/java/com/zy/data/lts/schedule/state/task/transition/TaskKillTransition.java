package com.zy.data.lts.schedule.state.task.transition;

import com.zy.data.lts.core.dao.TaskDao;
import com.zy.data.lts.core.entity.Task;
import com.zy.data.lts.core.model.KillTaskRequest;
import com.zy.data.lts.schedule.state.SingleArcTransition;
import com.zy.data.lts.schedule.state.task.MemTask;
import com.zy.data.lts.schedule.state.task.TaskEvent;
import com.zy.data.lts.schedule.state.task.TaskStatus;
import com.zy.data.lts.schedule.trigger.JobTrigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * Kill 当前task, 任务失败
 */
@Component
public class TaskKillTransition implements SingleArcTransition<MemTask, TaskEvent> {

    @Autowired
    TaskDao taskDao;

    @Autowired
    JobTrigger jobTrigger;

    @Override
    public void transition(MemTask memTask, TaskEvent event) {

        Task task = memTask.getTask();
        task.setTaskStatus(TaskStatus.Killed.code());
        taskDao.update(task);

        try {


            jobTrigger.killTask(new KillTaskRequest(task.getHost(),
                    task.getFlowTaskId(),
                    task.getTaskId(),
                    0));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}