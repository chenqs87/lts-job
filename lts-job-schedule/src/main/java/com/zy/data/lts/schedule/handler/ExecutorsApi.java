package com.zy.data.lts.schedule.handler;

import com.zy.data.lts.core.api.IExecutorApi;
import com.zy.data.lts.core.model.BeatInfoRequest;
import com.zy.data.lts.core.model.ExecuteRequest;
import com.zy.data.lts.core.model.Executor;
import com.zy.data.lts.core.model.KillTaskRequest;
import com.zy.data.lts.core.tool.SpringContext;
import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author chenqingsong
 * @date 2019/5/13 10:23
 */
@Component
public class ExecutorsApi implements IExecutorApi {

    //<handlerName, HandlerAPI>
    private final Map<String, HandlerApi> handlerApiMap = new ConcurrentHashMap<>();

    //<host:port, Executor>
    private final Map<String, Executor> executorMap = new ConcurrentHashMap<>();

    @Autowired
    SpringContext springContext;


    public void beat(BeatInfoRequest beat) {
        if (beat.getPort() > 0) {
            String host = beat.getHost() + ":" + beat.getPort();
            updateExecutors(beat, host);
        }
    }

    private void updateExecutors(BeatInfoRequest beat, String host) {
        String hostAndPort = beat.getHost() + ":" + beat.getPort();
        // 更新executor 心跳时间
        executorMap.computeIfPresent(hostAndPort, (k, v) -> {
            v.setLastUpdateTime(System.currentTimeMillis());
            return v;
        });

        // 新增executors
        Executor executor = executorMap.computeIfAbsent(hostAndPort, f -> createExecutor(beat, host));

        HandlerApi handlerApi = handlerApiMap.computeIfAbsent(beat.getHandler(), f -> {
            synchronized (ExecutorsApi.this) {
                ExecutorsApi.this.notifyAll();
                return new HandlerApi(new RoundRobinHandler(beat.getHandler()), springContext.getApplicationContext());
            }
        });

        handlerApi.beat(executor);


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
    public void execute(ExecuteRequest request) {
        HandlerApi handlerApi = handlerApiMap.get(request.getHandler());

        if (handlerApi == null) {
            throw new IllegalArgumentException("Handler [" + request.getHandler() + "] is not exist!");
        }

        handlerApi.execute(request);
    }

    @Override
    public void kill(KillTaskRequest request) {
        Executor executor = executorMap.get(request.getHost());

        if (executor == null) {
            return;
        }

        HandlerApi handlerApi = handlerApiMap.get(executor.getHandler());

        if (handlerApi != null) {
            handlerApi.kill(request);
        }
    }

    public Map<String, Set<String>> getActiveExecutors() {
        // <handler,<ip:port>
        Map<String, Set<String>> map = new HashMap<>();

        executorMap.values().forEach(executor -> {
            Set<String> hosts = map.computeIfAbsent(executor.getHandler(), f -> new HashSet<>());
            if (executor.isActive()) {
                hosts.add(executor.getHost());
            }
        });

        return map;
    }

    @PreDestroy
    public void destroy() {
        handlerApiMap.values().forEach(HandlerApi::close);
    }
}
