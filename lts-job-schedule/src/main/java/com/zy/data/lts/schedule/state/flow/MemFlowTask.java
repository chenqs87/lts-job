package com.zy.data.lts.schedule.state.flow;

import com.zy.data.lts.core.entity.FlowTask;
import com.zy.data.lts.core.tool.SpringContext;
import com.zy.data.lts.schedule.state.EventHandler;
import com.zy.data.lts.schedule.state.StateMachine;
import com.zy.data.lts.schedule.state.StateMachineFactory;
import com.zy.data.lts.schedule.state.flow.transition.*;
import com.zy.data.lts.schedule.state.task.MemTask;
import com.zy.data.lts.schedule.state.task.TaskStatus;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author chenqingsong
 * @date 2019/4/2 15:33
 */
public class MemFlowTask extends ReentrantLock implements EventHandler<FlowEvent> {

    private final StateMachineFactory<MemFlowTask, FlowTaskStatus, FlowEventType, FlowEvent> stateMachineFactory;
    private final StateMachine<FlowTaskStatus, FlowEventType, FlowEvent> stateMachine;

    //执行完毕之后移除掉
    private Map<Integer, MemTask> memTasks = new HashMap<>();

    private FlowTask flowTask;


    public MemFlowTask(FlowTask flowTask, List<MemTask> tasks, SpringContext springContext) {
        this.flowTask = flowTask;

        tasks.forEach(task -> {
            TaskStatus status = TaskStatus.parse(task.getTask().getTaskStatus());
            if (!status.isFinish()) {
                memTasks.putIfAbsent(task.getTask().getTaskId(), task);
            }
        });

        stateMachineFactory = new StateMachineFactory<MemFlowTask, FlowTaskStatus, FlowEventType, FlowEvent>(FlowTaskStatus.parse(flowTask.getStatus()))
                .addTransition(FlowTaskStatus.New, FlowTaskStatus.Pending, FlowEventType.Submit,
                        springContext.getBean(FlowSubmitTransition.class))
                .addTransition(FlowTaskStatus.Pending, FlowTaskStatus.Running, FlowEventType.Execute,
                        springContext.getBean(FlowExecuteTransition.class))
                .addTransition(FlowTaskStatus.Running, FlowTaskStatus.Running, FlowEventType.Execute,
                        springContext.getBean(FlowExecuteTransition.class))
                .addTransition(FlowTaskStatus.Pending, FlowTaskStatus.Killed, FlowEventType.Kill,
                        springContext.getBean(FlowKillTransition.class))
                .addTransition(FlowTaskStatus.Running, FlowTaskStatus.Killed, FlowEventType.Kill,
                        springContext.getBean(FlowKillTransition.class))
                .addTransition(FlowTaskStatus.Running, EnumSet.of(FlowTaskStatus.Finished, FlowTaskStatus.Running),
                        FlowEventType.Finish, springContext.getBean(FlowFinishTransition.class))
                .addTransition(FlowTaskStatus.Running, FlowTaskStatus.Failed, FlowEventType.Fail,
                        springContext.getBean(FlowFailTransition.class))
                .installTopology();

        stateMachine = stateMachineFactory.make(this);
    }

    public MemTask getMemTask(int taskId) {
        return memTasks.get(taskId);
    }

    public boolean isFinished() {
        return memTasks.size() == 0;
    }

    @Override
    public void handle(FlowEvent event) {
        lock();
        try {
            stateMachine.doTransition(event.getType(), event);
        } finally {
            unlock();
        }
    }

    public void finishTask(int taskId) {
        memTasks.remove(taskId);
    }

    public Collection<MemTask> getTasks() {
        return memTasks.values();
    }

    public void clearTasks() {
        memTasks.clear();
    }

    public FlowTask getFlowTask() {
        return flowTask;
    }


}
