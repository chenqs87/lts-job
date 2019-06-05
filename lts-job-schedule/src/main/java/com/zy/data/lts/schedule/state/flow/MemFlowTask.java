package com.zy.data.lts.schedule.state.flow;

import com.zy.data.lts.core.FlowTaskStatus;
import com.zy.data.lts.core.entity.FlowTask;
import com.zy.data.lts.core.tool.SpringContext;
import com.zy.data.lts.schedule.state.EventHandler;
import com.zy.data.lts.schedule.state.StateMachine;
import com.zy.data.lts.schedule.state.StateMachineFactory;
import com.zy.data.lts.schedule.state.flow.transition.*;
import com.zy.data.lts.schedule.state.task.MemTask;
import com.zy.data.lts.core.TaskStatus;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 内存作业对象，主要负责内存中工作流的状态变更，状态变更必须遵循状态变更的顺序，该顺序由线程池队列和公平锁来保证。
 * 正常情况下，先执行的状态变更操作先入线程队列，先执行，但是高并发极端情况下，先后进入线程队列的作业在执行状态变更时，
 * 会同时执行lock加锁操作，如果采用非公平锁，有可能后入线程池队列的状态变更操作优先获得锁，优先执行，这样会直接引起状态机状态异常，
 * 所以在这里 MemFlowTask 继承 ReentrantLock， 并采用公平锁，保证优先申请获取锁操作的行为，优先获取到锁，优先执行。
 *
 * @author chenqingsong
 * @date 2019/4/2 15:33
 */
public class MemFlowTask extends ReentrantLock implements EventHandler<FlowEvent> {

    private final StateMachineFactory<MemFlowTask, FlowTaskStatus, FlowEventType, FlowEvent> stateMachineFactory;
    private final StateMachine<FlowTaskStatus, FlowEventType, FlowEvent> stateMachine;

    //执行完毕之后移除掉
    private Map<Integer, MemTask> memTasks = new HashMap<>();

    private FlowTask flowTask;

    public MemFlowTask(FlowTask flowTask, List<MemTask> tasks) {
        super(true);
        this.flowTask = flowTask;

        tasks.forEach(task -> {
            TaskStatus status = TaskStatus.parse(task.getTask().getTaskStatus());
            if (!status.isFinish()) {
                memTasks.putIfAbsent(task.getTask().getTaskId(), task);
            }
        });

        stateMachineFactory = new StateMachineFactory<MemFlowTask, FlowTaskStatus, FlowEventType, FlowEvent>(FlowTaskStatus.parse(flowTask.getStatus()))
                .addTransition(FlowTaskStatus.New, FlowTaskStatus.Pending, FlowEventType.Submit,
                        SpringContext.getBean(FlowSubmitTransition.class))
                .addTransition(FlowTaskStatus.Pending, FlowTaskStatus.Running, FlowEventType.Execute,
                        SpringContext.getBean(FlowExecuteTransition.class))
                .addTransition(FlowTaskStatus.Running, FlowTaskStatus.Running, FlowEventType.Execute,
                        SpringContext.getBean(FlowExecuteTransition.class))
                .addTransition(FlowTaskStatus.Pending, FlowTaskStatus.Killed, FlowEventType.Kill,
                        SpringContext.getBean(FlowKillTransition.class))
                .addTransition(FlowTaskStatus.Running, FlowTaskStatus.Killed, FlowEventType.Kill,
                        SpringContext.getBean(FlowKillTransition.class))
                .addTransition(FlowTaskStatus.Running, EnumSet.of(FlowTaskStatus.Finished, FlowTaskStatus.Running),
                        FlowEventType.Finish, SpringContext.getBean(FlowFinishTransition.class))
                .addTransition(FlowTaskStatus.Running, FlowTaskStatus.Failed, FlowEventType.Fail,
                        SpringContext.getBean(FlowFailTransition.class))
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
