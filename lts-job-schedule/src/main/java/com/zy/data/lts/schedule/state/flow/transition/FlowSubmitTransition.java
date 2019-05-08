package com.zy.data.lts.schedule.state.flow.transition;

import com.zy.data.lts.core.dao.FlowTaskDao;
import com.zy.data.lts.core.entity.FlowTask;
import com.zy.data.lts.schedule.state.SingleArcTransition;
import com.zy.data.lts.schedule.state.flow.FlowEvent;
import com.zy.data.lts.schedule.state.flow.FlowTaskStatus;
import com.zy.data.lts.schedule.state.flow.MemFlowTask;
import com.zy.data.lts.schedule.state.task.TaskEvent;
import com.zy.data.lts.schedule.state.task.TaskEventType;
import com.zy.data.lts.schedule.state.task.TaskStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FlowSubmitTransition implements SingleArcTransition<MemFlowTask, FlowEvent> {

    @Autowired
    FlowTaskDao flowTaskDao;

    @Override
    public void transition(MemFlowTask memFlowTask, FlowEvent event) {
        memFlowTask.lock();
        try {
            memFlowTask.getTasks().forEach(t -> {
                t.handle(new TaskEvent(TaskEventType.Submit));
                if (t.getCurrentStatus() == TaskStatus.Pending) {
                    t.handle(new TaskEvent(TaskEventType.Send));
                }
            });

            FlowTask flowTask = memFlowTask.getFlowTask();
            flowTask.setStatus(FlowTaskStatus.Pending.code());
            flowTaskDao.update(flowTask);
        } finally {
            memFlowTask.unlock();
        }
    }
}