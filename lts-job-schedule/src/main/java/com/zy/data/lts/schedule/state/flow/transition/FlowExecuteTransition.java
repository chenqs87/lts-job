package com.zy.data.lts.schedule.state.flow.transition;

import com.zy.data.lts.core.dao.FlowTaskDao;
import com.zy.data.lts.core.entity.FlowTask;
import com.zy.data.lts.schedule.state.SingleArcTransition;
import com.zy.data.lts.schedule.state.flow.FlowEvent;
import com.zy.data.lts.schedule.state.flow.FlowTaskStatus;
import com.zy.data.lts.schedule.state.flow.MemFlowTask;
import com.zy.data.lts.schedule.state.task.TaskEvent;
import com.zy.data.lts.schedule.state.task.TaskEventType;
import com.zy.data.lts.schedule.trigger.JobTrigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FlowExecuteTransition implements SingleArcTransition<MemFlowTask, FlowEvent> {

    @Autowired
    FlowTaskDao flowTaskDao;

    @Autowired
    JobTrigger jobTrigger;

    @Override
    public void transition(MemFlowTask memFlowTask, FlowEvent flowEvent) {

        memFlowTask.lock();

        try {
            int taskId = flowEvent.getCurrentTaskId();
            if (taskId == -1) {
                jobTrigger.handleUnFinishFlow(memFlowTask);
            } else {

                memFlowTask.getMemTask(taskId).handle(new TaskEvent(TaskEventType.Execute));

                FlowTask ft = memFlowTask.getFlowTask();
                int status = FlowTaskStatus.Running.code();
                if (ft.getStatus() == status) {
                    return;
                }

                ft.setStatus(status);
                flowTaskDao.update(ft);
            }
        } finally {
            memFlowTask.unlock();
        }
    }
}