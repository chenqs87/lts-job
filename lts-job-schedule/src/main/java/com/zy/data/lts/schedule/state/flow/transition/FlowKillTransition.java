package com.zy.data.lts.schedule.state.flow.transition;

import com.zy.data.lts.core.dao.FlowTaskDao;
import com.zy.data.lts.core.entity.FlowTask;
import com.zy.data.lts.schedule.alert.CommonAlerter;
import com.zy.data.lts.schedule.state.SingleArcTransition;
import com.zy.data.lts.schedule.state.flow.FlowEvent;
import com.zy.data.lts.core.FlowTaskStatus;
import com.zy.data.lts.schedule.state.flow.MemFlowTask;
import com.zy.data.lts.schedule.state.task.TaskEvent;
import com.zy.data.lts.schedule.state.task.TaskEventType;
import com.zy.data.lts.core.TaskStatus;
import com.zy.data.lts.schedule.trigger.JobTrigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class FlowKillTransition implements SingleArcTransition<MemFlowTask, FlowEvent> {

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
            // 同步操作，kill 完成后，清理
            memFlowTask.getTasks().forEach(t -> {
                switch (TaskStatus.parse(t.getTask().getTaskStatus())) {
                    case Ready:
                    case Pending:
                    case Submitted:
                    case Running:
                        t.handle(new TaskEvent(TaskEventType.Kill));
                }
            });
            memFlowTask.clearTasks();

            FlowTask ft = memFlowTask.getFlowTask();
            ft.setStatus(FlowTaskStatus.Killed.getCode());
            flowTaskDao.update(ft);
            jobTrigger.finishFlowTask(ft.getId());
            alerter.failed(memFlowTask.getFlowTask());
        } finally {
            memFlowTask.unlock();
        }
    }
}