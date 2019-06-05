package com.zy.data.lts.schedule.state.flow.transition;

import com.zy.data.lts.core.dao.FlowTaskDao;
import com.zy.data.lts.core.entity.FlowTask;
import com.zy.data.lts.schedule.alert.CommonAlerter;
import com.zy.data.lts.schedule.state.SingleArcTransition;
import com.zy.data.lts.schedule.state.flow.FlowEvent;
import com.zy.data.lts.core.FlowTaskStatus;
import com.zy.data.lts.schedule.state.flow.MemFlowTask;
import com.zy.data.lts.schedule.state.task.MemTask;
import com.zy.data.lts.schedule.state.task.TaskEvent;
import com.zy.data.lts.schedule.state.task.TaskEventType;
import com.zy.data.lts.schedule.trigger.JobTrigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class FlowFailTransition implements SingleArcTransition<MemFlowTask, FlowEvent> {

    @Autowired
    FlowTaskDao flowTaskDao;

    @Lazy
    @Autowired
    JobTrigger jobTrigger;

    @Autowired
    CommonAlerter alerter;

    @Override
    public void transition(MemFlowTask memFlowTask, FlowEvent flowEvent) {
        memFlowTask.lock();
        try {
            int taskId = flowEvent.getCurrentTaskId();

            MemTask memTask = memFlowTask.getMemTask(taskId);
            memTask.handle(new TaskEvent(TaskEventType.Fail));
            memFlowTask.finishTask(taskId);

            memFlowTask.getTasks().forEach(t -> t.handle(new TaskEvent(TaskEventType.Kill)));
            memFlowTask.clearTasks();

            FlowTask ft = memFlowTask.getFlowTask();
            ft.setStatus(FlowTaskStatus.Failed.code());
            flowTaskDao.update(ft);

            jobTrigger.finishFlowTask(ft.getId());
            alerter.failed(memFlowTask.getFlowTask());
        } finally {
            memFlowTask.unlock();
        }
    }
}