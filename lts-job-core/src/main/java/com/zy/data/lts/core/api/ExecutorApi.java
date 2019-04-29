package com.zy.data.lts.core.api;

import com.zy.data.lts.core.model.BeatInfoRequest;
import com.zy.data.lts.core.model.ExecuteRequest;
import com.zy.data.lts.core.model.Executor;
import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author chenqingsong
 * @date 2019/4/1 13:44
 */
@Component
@ConditionalOnProperty(name= "lts.server.role", havingValue = "admin")
public class ExecutorApi implements IExecutorApi {

    //<ip:port,executor>
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
        String host = beat.getHost() + ":" + beat.getPort();

        updateExecutors(beat, host);
    }

    @PreDestroy
    private void destroy() {
        isRunning.set(false);

    }

    private void doExec() {
        try {
            for (Executor executor : executors.values()) {
                if (executor.isActive()) {
                    ExecuteRequest request = queue.poll(5, TimeUnit.SECONDS);
                    if(request != null && executor.getHandler().equals(request.getHandler())) {
                        try {
                            executor.getApi().execute(request);
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

    private void updateExecutors(BeatInfoRequest beat, String host) {
        String hostAndPort = beat.getHost() + ":" + beat.getPort();
        // 更新executor 心跳时间
        executors.computeIfPresent(hostAndPort, (k, v)  -> {
            v.setLastUpdateTime(System.currentTimeMillis());
            return v;
        });

        // 新增executors
        executors.computeIfAbsent(hostAndPort, f -> {
            Executor executor = new Executor();

            IExecutorApi executorApi = Feign.builder()
                    .encoder(new GsonEncoder())
                    .decoder(new GsonDecoder())
                    .target(IExecutorApi.class, "http://" + beat.getHost() + ":" + beat.getPort());
            executor.setApi(executorApi);
            executor.setHost(host);
            executor.setHandler(beat.getHandler());
            return executor;
        });
    }


}
