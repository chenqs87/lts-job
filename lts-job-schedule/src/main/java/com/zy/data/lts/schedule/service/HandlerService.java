package com.zy.data.lts.schedule.service;

import com.zy.data.lts.core.api.IExecutor;
import com.zy.data.lts.core.dao.JobDao;
import com.zy.data.lts.core.entity.Job;
import com.zy.data.lts.core.model.BeatInfoRequest;
import com.zy.data.lts.core.model.ExecuteRequest;
import com.zy.data.lts.core.model.KillTaskRequest;
import com.zy.data.lts.core.tool.SpringContext;
import com.zy.data.lts.naming.handler.LocalHandlerManager;
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

/**
 * @author chenqingsong
 * @date 2019/5/13 10:23
 */
@Component
public class HandlerService implements IExecutor {

    private static final Logger logger = LoggerFactory.getLogger(HandlerService.class);

    @Autowired(required = false)
    private LocalHandlerManager localHandlerManager;

    @Autowired
    private JobDao jobDao;

    public void beat(BeatInfoRequest beat) {
        if (beat.getPort() > 0) {
            localHandlerManager.beat(beat);
        }
    }

    @Override
    public void execute(ExecuteRequest request) {
        getHandler(request.getHandler()).execute(request);
    }

    private AsyncHandler getHandler(String handlerName) {
        return SpringContext.getBeanByName(
                handlerName + AsyncHandler.class.getSimpleName(), AsyncHandler.class);
    }

    @Override
    public void kill(KillTaskRequest request) {
        int jobId = request.getTask().getJobId();
        Job job = jobDao.findById(jobId);

        getHandler(job.getHandler()).kill(request);
    }

    public Map<String, Set<String>> getActiveExecutors() {
        // <handler,<ip:port>
        Map<String, Set<String>> map = new HashMap<>();

        Map<String, RoundRobinHandler> handlerMap =  SpringContext.getBeansByType(RoundRobinHandler.class);
        handlerMap.values().forEach(handler -> {
            Set<String> hosts = map.computeIfAbsent(handler.name(), f -> new HashSet<>());
            handler.getExecutors().forEach(executor -> hosts.add(executor.getHost()));
        });

        return map;
    }
}
