package com.zy.data.lts.schedule.state.task;

import com.zy.data.lts.core.entity.Task;
import com.zy.data.lts.core.tool.SpringContext;
import com.zy.data.lts.schedule.state.EventHandler;
import com.zy.data.lts.schedule.state.StateMachine;
import com.zy.data.lts.schedule.state.StateMachineFactory;
import com.zy.data.lts.schedule.state.task.transition.*;

import java.util.EnumSet;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author chenqingsong
 * @date 2019/4/2 12:22
 */
public class MemTask extends ReentrantLock implements EventHandler<TaskEvent> {

    private final StateMachineFactory<MemTask, TaskStatus, TaskEventType, TaskEvent> stateMachineFactory;
    private final StateMachine<TaskStatus, TaskEventType, TaskEvent> stateMachine;

    private Task task;
    private boolean isSkip;

    public MemTask(Task task) {

        if (task == null) {
            throw new IllegalArgumentException("task is null!!");
        }
        this.task = task;
        this.isSkip = false;
        stateMachineFactory
                = new StateMachineFactory<MemTask, TaskStatus, TaskEventType, TaskEvent>(TaskStatus.parse(task.getTaskStatus()))
                .addTransition(TaskStatus.New, EnumSet.of(TaskStatus.Ready, TaskStatus.Pending),
                        TaskEventType.Submit, SpringContext.getBean(TaskPendTransition.class))
                .addTransition(TaskStatus.Ready, EnumSet.of(TaskStatus.Ready, TaskStatus.Pending), TaskEventType.Pend,
                        SpringContext.getBean(TaskPendTransition.class))
                .addTransition(TaskStatus.Ready, TaskStatus.Killed, TaskEventType.Kill,
                        SpringContext.getBean(TaskKillTransition.class))
                .addTransition(TaskStatus.Pending, TaskStatus.Killed, TaskEventType.Kill,
                        SpringContext.getBean(TaskKillTransition.class))
                .addTransition(TaskStatus.Submitted, TaskStatus.Killed, TaskEventType.Kill,
                        SpringContext.getBean(TaskKillTransition.class))
                .addTransition(TaskStatus.Running, TaskStatus.Killed, TaskEventType.Kill,
                        SpringContext.getBean(TaskKillTransition.class))
                .addTransition(TaskStatus.Pending, TaskStatus.Submitted, TaskEventType.Send,
                        SpringContext.getBean(TaskSendTransition.class))
                .addTransition(TaskStatus.Submitted, TaskStatus.Running, TaskEventType.Execute,
                        SpringContext.getBean(TaskExecuteTransition.class))
                .addTransition(TaskStatus.Running, TaskStatus.Running, TaskEventType.Execute,
                        SpringContext.getBean(TaskExecuteTransition.class))
                .addTransition(TaskStatus.Running, EnumSet.of(TaskStatus.Finished, TaskStatus.Running),
                        TaskEventType.Finish, SpringContext.getBean(TaskFinishTransition.class))
                .addTransition(TaskStatus.Running, TaskStatus.Failed, TaskEventType.Fail,
                        SpringContext.getBean(TaskFailTransition.class))
                .installTopology();

        this.stateMachine = stateMachineFactory.make(this);
    }

    @Override
    public void handle(TaskEvent event) {
        lock();
        try {
            stateMachine.doTransition(event.getType(), event);
        } finally {
            unlock();
        }
    }

    public TaskStatus getCurrentStatus() {
        return stateMachine.getCurrentState();
    }

    public Task getTask() {
        return this.task;
    }

    public boolean isSkip() {
        return isSkip;
    }

    public void setSkip(boolean skip) {
        isSkip = skip;
    }
}
