package com.zy.data.lts.schedule.handler;

import com.zy.data.lts.core.model.ExecuteRequest;
import com.zy.data.lts.core.model.Executor;
import com.zy.data.lts.core.model.KillTaskRequest;
import com.zy.data.lts.core.model.UpdateTaskHostEvent;
import com.zy.data.lts.core.tool.SpringContext;
import feign.RetryableException;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class RoundRobinHandler implements IHandler {
    private Logger logger = LoggerFactory.getLogger(RoundRobinHandler.class);

    private static final int MAX_INDEX = 20;

    private final AtomicBoolean isRunning = new AtomicBoolean(true);

    // <ip:port, executor>
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


    public void beat(Executor executor) {

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
                SpringContext.getApplicationContext().publishEvent(
                        new UpdateTaskHostEvent(request.getFlowTaskId(), request.getTaskId(), executor.getHost()));

                executor.execute(request);
            } catch (Exception e) {
                //失败重发
                logger.warn("Fail to asyncExec Job", e);
                if (e instanceof RetryableException) {
                    remove(executor.getHost());
                }

                execute(request);
            }
        });

    }

    @Override
    public void kill(KillTaskRequest request) {

        doExec(executor -> {
            try {
                executor.kill(request);
            } catch (Exception e) {
                logger.warn("Fail to kill the job [{}] ", request, e);
            }
        });
    }
}
