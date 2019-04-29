package com.zy.data.lts.schedule.state.flow.transition;

import com.zy.data.lts.core.dao.FlowTaskDao;
import com.zy.data.lts.core.entity.FlowTask;
import com.zy.data.lts.schedule.state.flow.FlowTaskStatus;
import com.zy.data.lts.schedule.state.SingleArcTransition;
import com.zy.data.lts.schedule.state.flow.FlowEvent;
import com.zy.data.lts.schedule.state.flow.MemFlowTask;
import com.zy.data.lts.schedule.state.task.TaskEvent;
import com.zy.data.lts.schedule.state.task.TaskEventType;
import com.zy.data.lts.schedule.trigger.JobTrigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FlowKillTransition implements SingleArcTransition<MemFlowTask, FlowEvent> {

    @Autowired
    FlowTaskDao flowTaskDao;

    @Autowired
    JobTrigger jobTrigger;


    @Override
    public void transition(MemFlowTask memFlowTask, FlowEvent flowEvent) {
        memFlowTask.lock();
        try {
            // 同步操作，kill 完成后，清理
            memFlowTask.getTasks().forEach(t -> t.handle(new TaskEvent(TaskEventType.Kill)) );
            memFlowTask.clearTasks();

            FlowTask ft = memFlowTask.getFlowTask();
            ft.setStatus(FlowTaskStatus.Killed.code());
            flowTaskDao.update(ft);
            jobTrigger.finishFlowTask(ft.getId());
        } finally {
            memFlowTask.unlock();
        }
    }
}