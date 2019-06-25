package com.zy.data.lts.executor.monitor;

import com.zy.data.lts.executor.service.CommandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Endpoint(id = "tasks")
public class TaskEndpoint {

    @Autowired
    private CommandService commandService;

    @ReadOperation
    public List<Map<String, String>> running() {
        return commandService.getRunningTasks();
    }

    @ReadOperation
    public List<Map<String, String>> taskRunning(@Selector String flowTaskId) {
        return commandService.getRunningTasks(flowTaskId);
    }

}