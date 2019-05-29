package com.zy.data.lts.schedule.handler;

import com.zy.data.lts.core.api.IExecutorApi;
import com.zy.data.lts.core.model.BeatInfoRequest;
import com.zy.data.lts.core.model.ExecuteRequest;
import com.zy.data.lts.core.model.Executor;
import com.zy.data.lts.core.model.KillTaskRequest;
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
public class HandlerService implements IExecutorApi {


    @Autowired
    private GsonEncoder gsonEncoder;

    @Autowired
    private GsonDecoder gsonDecoder;


    //<handlerName, HandlerAPI>
    private final Map<String, AsyncHandler> handlerApiMap = new ConcurrentHashMap<>();

    //<host:port, Executor>
    private final Map<String, Executor> executorMap = new ConcurrentHashMap<>();


    public void beat(BeatInfoRequest beat) {
        if (beat.getPort() > 0) {
            updateExecutors(beat);
        }
    }

    private void updateExecutors(BeatInfoRequest beat) {
        String hostAndPort = beat.getHost() + ":" + beat.getPort();
        // 更新executor 心跳时间
        executorMap.computeIfPresent(hostAndPort, (k, v) -> {
            v.setLastUpdateTime(System.currentTimeMillis());
            return v;
        });

        // 新增executors
        Executor executor = executorMap.computeIfAbsent(hostAndPort, f -> createExecutor(beat));

        AsyncHandler asyncHandler = handlerApiMap.computeIfAbsent(beat.getHandler(), f ->
            new AsyncHandler(new RoundRobinHandler(beat.getHandler())));

        asyncHandler.beat(executor);


    }

    private Executor createExecutor(BeatInfoRequest beat) {
        Executor executor = new Executor();
        String host = beat.getHost() + ":" + beat.getPort();
        IExecutorApi api = Feign.builder()
                .encoder(gsonEncoder)
                .decoder(gsonDecoder)
                .target(IExecutorApi.class, "http://" + host);
        executor.setApi(api);
        executor.setHost(host);
        executor.setHandler(beat.getHandler());
        return executor;
    }

    @Override
    public void execute(ExecuteRequest request) {
        AsyncHandler asyncHandler = handlerApiMap.get(request.getHandler());

        if (asyncHandler == null) {
            throw new IllegalArgumentException("AsyncHandler [" + request.getHandler() + "] is not exist!");
        }

        asyncHandler.execute(request);
    }

    @Override
    public void kill(KillTaskRequest request) {
        Executor executor = executorMap.get(request.getHost());

        if (executor == null) {
            return;
        }

        AsyncHandler asyncHandler = handlerApiMap.get(executor.getHandler());

        if (asyncHandler != null) {
            asyncHandler.kill(request);
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
        handlerApiMap.values().forEach(AsyncHandler::close);
    }

    public void registerBean(BeatInfoRequest beat) {
        if(beat.getPort() == 0) {
            return;
        }

        Executor executor = createExecutor(beat);

        String host = beat.getHost() + ":" + beat.getPort();
       // SpringContext.getApplicationContext().getBeanFactory().
    }
}
