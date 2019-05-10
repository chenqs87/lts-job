package com.zy.data.lts.core.model;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author chenqingsong
 * @date 2019/5/8 20:30
 */
public class Handler {

    private static final int MAX_INDEX = 1000;
    // <ip:port, executor>
    private final Map<String, Executor> executorMap = new ConcurrentHashMap<>();

    private final List<String> executors = new CopyOnWriteArrayList<>();

    private final AtomicInteger index ;

    public Handler() {
        index = new AtomicInteger(0);
    }

    public void addExecutor(Executor executor) {

        executorMap.computeIfAbsent(executor.getHost(), f -> {
            executors.add(executor.getHost());
            return executor;
        });

        synchronized (this) {
            this.notifyAll();
        }
    }



    public void remove(String host) {
        executorMap.remove(host);
        executors.remove(host);
    }

    public int nextIndex() {
        do {
            int current = index.get();
            int next  =  current + 1;
            if(next > MAX_INDEX || next < 0) {
                next = 0;
            }

            if(index.compareAndSet(current, next)) {
                return next;
            }
        } while (true);


    }

    public Executor nextExecutor() throws InterruptedException {

        int count = executors.size();
        do {
            int current =  nextIndex();
            int executorIndex = current % count;
            String key =  executors.get(executorIndex);
            Executor executor =  executorMap.get(key);

            if(executor == null) {
                executors.remove(executorIndex);
            } else {
                return executor;
            }

            if(--count < 1) {
                synchronized (this) {
                    wait();
                }
            }

        } while (true);

    }

}
