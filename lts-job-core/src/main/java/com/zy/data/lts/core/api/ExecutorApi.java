package com.zy.data.lts.core.api;

import com.zy.data.lts.core.model.*;
import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author chenqingsong
 * @date 2019/4/1 13:44
 */
@Component
@ConditionalOnProperty(name= "lts.server.role", havingValue = "admin")
public class ExecutorApi implements IExecutorApi, ApplicationContextAware {

    private ApplicationContext applicationContext;

    //<ip:port, executor>
    private final Map<String, Executor> executors = new ConcurrentHashMap<>();

    private final BlockingQueue<ExecuteRequest> queue = new LinkedBlockingQueue<>();

    private final AtomicBoolean isRunning = new AtomicBoolean(true);

    @PostConstruct
    public void init() {
        new Thread(() -> {
            while (isRunning.get()) {
                doExec();
            }
        }).start();

    }
    public void refresh(BeatInfoRequest beat) {
        if(beat.getPort() > 0) {
            String host = beat.getHost() + ":" + beat.getPort();
            updateExecutors(beat, host);
        }
    }

    @PreDestroy
    private void destroy() {
        isRunning.set(false);
    }

    private void doExec() {
        try {
            for (Executor executor : executors.values()) {
                if (executor.isActive()) {
                    ExecuteRequest request = queue.poll(2, TimeUnit.SECONDS);
                    if(request != null && executor.getHandler().equals(request.getHandler())) {
                        try {
                            executor.getApi().execute(request);
                            applicationContext.publishEvent(
                                    new UpdateTaskHostEvent(request.getFlowTaskId(),
                                            request.getTaskId(), executor.getHost()));
                        } catch (Exception e) {
                            e.printStackTrace();
                            queue.put(request);
                        }

                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void execute(ExecuteRequest request) {
        queue.offer(request);
    }

    @Override
    public void kill(KillTaskRequest request) {
        Executor executor = executors.get(request.getHost());
        if(executor != null) {
            executor.getApi().kill(request);
        }
    }

    private void updateExecutors(BeatInfoRequest beat, String host) {
        String hostAndPort = beat.getHost() + ":" + beat.getPort();
        // 更新executor 心跳时间
        executors.computeIfPresent(hostAndPort, (k, v)  -> {
            v.setLastUpdateTime(System.currentTimeMillis());
            return v;
        });

        // 新增executors
        executors.computeIfAbsent(hostAndPort, f -> createExecutor(beat, host));
    }

    private Executor createExecutor(BeatInfoRequest beat, String host) {
        Executor executor = new Executor();

        IExecutorApi executorApi = Feign.builder()
                .encoder(new GsonEncoder())
                .decoder(new GsonDecoder())
                .target(IExecutorApi.class, "http://" + beat.getHost() + ":" + beat.getPort());
        executor.setApi(executorApi);
        executor.setHost(host);
        executor.setHandler(beat.getHandler());
        return executor;
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public Map<String, Set<String>> getActiveExecutors() {
        // <handler,<ip:port>
        Map<String, Set<String>> map = new HashMap<>();

        executors.values().forEach(executor -> {
            Set<String> hosts = map.computeIfAbsent(executor.getHandler(), f -> new HashSet<>());
            if(executor.isActive()) {
                hosts.add(executor.getHost());
            }
        });

       return map;
    }
}
