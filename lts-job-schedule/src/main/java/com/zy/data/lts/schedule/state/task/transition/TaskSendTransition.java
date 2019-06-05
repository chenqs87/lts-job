package com.zy.data.lts.schedule.state.task.transition;

import com.zy.data.lts.core.dao.TaskDao;
import com.zy.data.lts.core.entity.Task;
import com.zy.data.lts.schedule.state.SingleArcTransition;
import com.zy.data.lts.schedule.state.task.MemTask;
import com.zy.data.lts.schedule.state.task.TaskEvent;
import com.zy.data.lts.core.TaskStatus;
import com.zy.data.lts.schedule.trigger.JobTrigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class TaskSendTransition implements SingleArcTransition<MemTask, TaskEvent> {

    @Autowired
    TaskDao taskDao;

    @Autowired
    @Lazy
    JobTrigger jobTrigger;

    @Override
    public void transition(MemTask job, TaskEvent event) {
        Task task = job.getTask();
        task.setTaskStatus(TaskStatus.Submitted.code());
        taskDao.update(task);

        jobTrigger.sendTask(job);
    }
}