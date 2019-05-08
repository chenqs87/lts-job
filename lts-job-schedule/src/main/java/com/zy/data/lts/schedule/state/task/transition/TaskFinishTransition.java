package com.zy.data.lts.schedule.state.task.transition;

import com.zy.data.lts.core.dao.TaskDao;
import com.zy.data.lts.core.entity.Task;
import com.zy.data.lts.schedule.state.MultipleArcTransition;
import com.zy.data.lts.schedule.state.task.MemTask;
import com.zy.data.lts.schedule.state.task.TaskEvent;
import com.zy.data.lts.schedule.state.task.TaskStatus;
import com.zy.data.lts.schedule.trigger.JobTrigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class TaskFinishTransition implements MultipleArcTransition<MemTask, TaskEvent, TaskStatus> {

    @Autowired
    TaskDao taskDao;

    @Autowired
    JobTrigger jobTrigger;

    @Override
    public TaskStatus transition(MemTask memTask, TaskEvent event) {
        memTask.lock();
        try {
            Task task = memTask.getTask();
            task.completeShard(event.getShardStatus());
            TaskStatus ret = task.getShardStatus() == 0 ? TaskStatus.Finished : TaskStatus.Running;

            if(ret == TaskStatus.Finished) {
                task.setTaskStatus(ret.code());
                task.setEndTime(new Date());
                taskDao.update(task);
            }

            return ret;
        } finally {
            memTask.unlock();
        }
    }
}