package com.zy.data.lts.schedule.state.task.transition;

import com.zy.data.lts.core.dao.TaskDao;
import com.zy.data.lts.core.entity.Task;
import com.zy.data.lts.schedule.state.MultipleArcTransition;
import com.zy.data.lts.schedule.state.task.MemTask;
import com.zy.data.lts.schedule.state.task.TaskEvent;
import com.zy.data.lts.core.TaskStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TaskPendTransition implements MultipleArcTransition<MemTask, TaskEvent, TaskStatus> {

    @Autowired
    TaskDao taskDao;

    @Override
    public TaskStatus transition(MemTask job, TaskEvent event) {
        Task task = job.getTask();

        // 判断前置任务是否完成
        TaskStatus ret = task.getPreTask() == 0 ? TaskStatus.Pending : TaskStatus.Ready;
        task.setTaskStatus(ret.code());
        taskDao.update(task);

        return ret;
    }
}