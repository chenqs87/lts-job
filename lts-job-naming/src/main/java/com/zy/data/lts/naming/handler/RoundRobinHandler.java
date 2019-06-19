package com.zy.data.lts.naming.handler;

import com.zy.data.lts.core.model.*;
import com.zy.data.lts.core.tool.SpringContext;
import com.zy.data.lts.naming.ExecutorUnConnectedEvent;
import com.zy.data.lts.naming.LtsHandlerChangeEvent;
import feign.RetryableException;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Consumer;

/**
 * 轮询调度，依次对handler中的所有executor进行任务分配和调度，如果当前Handler中没有可用executor时，
 * 则阻塞所有请求，当有新的executor加入进来时，继续进行调度
 *
 * @author chenqingsong
 * @date 2019/5/8 20:30
 */
public class RoundRobinHandler implements IHandler, ApplicationListener<LtsHandlerChangeEvent> {
    private Logger logger = LoggerFactory.getLogger(RoundRobinHandler.class);

    private static final int MAX_INDEX = 20;

    private final AtomicBoolean isRunning = new AtomicBoolean(true);

    // <ip:port, handler>
    private final Map<String, Executor> executorMap = new ConcurrentHashMap<>();

    private final AtomicInteger index = new AtomicInteger(0);

    private final String handlerName;
    private final AtomicReferenceArray<String> virtualExecutors;
    private final int roundIndex;


    public RoundRobinHandler(String handlerName, int roundIndex) {
        this.handlerName = handlerName;
        this.roundIndex = roundIndex;
        virtualExecutors = new AtomicReferenceArray<>(roundIndex);

    }

    public String name() {
        return handlerName;
    }

    public RoundRobinHandler(String handlerName) {
        this(handlerName, MAX_INDEX);
    }

    private synchronized void reInstall() {
        if (executorMap.isEmpty()) {
            return;
        }

        Executor[] executors = executorMap.values()
                .stream()
                .filter(Executor::isActive)
                .toArray(Executor[]::new);

        if (ArrayUtils.isNotEmpty(executors)) {
            for (Executor executor : executors) {
                for (int j = 0; j < virtualExecutors.length(); j = j + executors.length) {
                    virtualExecutors.set(j, executor.getHost());
                }
            }
        }
    }


    public void addNew(Executor executor) {

        AtomicBoolean change = new AtomicBoolean(false);
        executorMap.computeIfAbsent(executor.getHost(), f -> {
            change.set(true);
            return executor;

        });

        if (change.get()) {
            reInstall();
            synchronized (this) {
                this.notifyAll();
            }
        }
    }

    public void remove(String host) {
        executorMap.remove(host);
        reInstall();
    }

    private int nextIndex() {
        do {
            int current = index.get();
            int next = current + 1;
            if (next > roundIndex || next < 0) {
                next = 0;
            }

            if (index.compareAndSet(current, next)) {
                return next;
            }
        } while (true);

    }

    public Executor nextExecutor() {

        int count = executorMap.size();
        do {
            int current = nextIndex();

            int executorIndex = current % roundIndex;
            String key = virtualExecutors.get(executorIndex);
            if (key != null) {
                Executor executor = executorMap.get(key);

                if (executor != null) {
                    if (executor.isActive() && executor.getHandler().equals(handlerName)) {
                        return executor;
                    } else {
                        // 当executor变更所属handler时，从原来的handler中删除
                        remove(executor.getHost());
                    }
                }
            }

            if (--count < 1) {
                synchronized (this) {
                    try {
                        wait();
                    } catch (InterruptedException ignore) {
                    }
                }
            }
        } while (isRunning.get());

        return null;

    }

    private void doExec(Consumer<Executor> consumer) {
        Executor executor = nextExecutor();
        // 如果executor 为空，说明当前进程准备关闭了，不再处理请求
        if (executor != null) {
            consumer.accept(executor);
        }
    }

    public List<Executor> getExecutors() {
        return new ArrayList<>(executorMap.values());
    }


    @Override
    public void close() {
        isRunning.set(false);
        synchronized (this) {
            this.notifyAll();
        }
    }

    @Override
    public void execute(ExecuteRequest request) {
        doExec(executor -> {
            try {
                SpringContext.publishEvent(new ExecLogEvent(request.getFlowTaskId(),
                        "Send task ["+request.getTaskId()+"] to [" + executor.getHost() + "]!"));
                SpringContext.publishEvent(
                        new UpdateTaskHostEvent(request.getFlowTaskId(), request.getTaskId(), executor.getHost()));

                executor.execute(request);

            } catch (Exception e) {

                //失败重发
                logger.warn("Fail to asyncExec Job", e);

                if (e instanceof RetryableException) {
                    SpringContext.publishEvent(new ExecutorUnConnectedEvent(executor));
                }

                SpringContext.publishEvent(new ExecLogEvent(request.getFlowTaskId(),
                        "Fail to send task to Executor [" + executor.getHost() + "] and ready to retry again!!!"));
                execute(request);
            }
        });
    }

    @Override
    public void kill(KillTaskRequest request) {
        Executor executor = executorMap.get(request.getHost());

        if(executor == null) {
            SpringContext.publishEvent(new ExecLogEvent(request.getFlowTaskId(),
                    "Fail to kill task! Executor ["+request.getHost()+"] is not exist!"));
            return;
        }

        try {
            executor.kill(request);
            SpringContext.publishEvent(new ExecLogEvent(request.getFlowTaskId(),
                    "Success to send kill task request to Executor [" + request.getHost() + "]"));
        } catch (Exception e) {
            SpringContext.publishEvent(new ExecLogEvent(request.getFlowTaskId(),
                    "Fail to kill task! msg: " + e.getMessage()));
            logger.warn("Fail to kill the job [{}] ", request, e);
        }
    }

    /**
     * 广播形式触发，符合条件的handler触发即可
     * @param event
     */
    @Override
    public void onApplicationEvent(LtsHandlerChangeEvent event) {
        if(!event.getHandlerName().equals(this.handlerName)) {
            return;
        }

        switch (event.getHandlerEventType()) {
            case NEW: addNew(event.getExecutor()); break;
            case DELETE: remove(event.getExecutor().getHost()); break;
        }
    }
}
