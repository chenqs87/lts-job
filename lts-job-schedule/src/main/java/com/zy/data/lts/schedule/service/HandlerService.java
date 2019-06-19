package com.zy.data.lts.schedule.service;

import com.zy.data.lts.core.api.IExecutorApi;
import com.zy.data.lts.core.model.BeatInfoRequest;
import com.zy.data.lts.core.model.ExecuteRequest;
import com.zy.data.lts.core.model.Executor;
import com.zy.data.lts.core.model.KillTaskRequest;
import com.zy.data.lts.core.tool.SpringContext;
import com.zy.data.lts.naming.LocalHandlerManager;
import com.zy.data.lts.naming.handler.AsyncHandler;
import com.zy.data.lts.naming.handler.RoundRobinHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    private static final Logger logger = LoggerFactory.getLogger(HandlerService.class);

    @Autowired(required = false)
    private LocalHandlerManager localHandlerManager;


    //<handlerName, HandlerAPI>
    private final Map<String, AsyncHandler> handlerApiMap = new ConcurrentHashMap<>();

    //<host:port, Executor>
    private final Map<String, Executor> executorMap = new ConcurrentHashMap<>();


    public void beat(BeatInfoRequest beat) {
        if (beat.getPort() > 0) {
            localHandlerManager.beat(beat);
        }
    }

    @Override
    public void execute(ExecuteRequest request) {
        AsyncHandler asyncHandler = SpringContext.getBeanByName(
                request.getHandler() + AsyncHandler.class.getName(), AsyncHandler.class);
        asyncHandler.execute(request);
    }

    @Override
    public void kill(KillTaskRequest request) {
        /*AsyncHandler asyncHandler = SpringContext.getBeanByName(
                request.getHandler() + AsyncHandler.class.getName(), AsyncHandler.class);*/

    }

    public Map<String, Set<String>> getActiveExecutors() {
        // <handler,<ip:port>
        Map<String, Set<String>> map = new HashMap<>();

        Map<String, RoundRobinHandler> handlerMap =  SpringContext.getBeansByType(RoundRobinHandler.class);
        handlerMap.values().forEach(handler -> {
            Set<String> hosts = map.computeIfAbsent(handler.name(), f -> new HashSet<>());
            handler.getExecutors().forEach(executor -> {
                hosts.add(executor.getHost());
            });
        });

        return map;
    }
}
