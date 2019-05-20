package com.zy.data.lts.executor.controller;

import com.zy.data.lts.core.model.ExecuteRequest;
import com.zy.data.lts.core.model.KillTaskRequest;
import com.zy.data.lts.executor.service.JobService;
import com.zy.data.lts.executor.service.LogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author chenqingsong
 * @date 2019/4/9 16:46
 */
@Api(description = "Executor接口")
@RestController
@RequestMapping("/executor")
public class ExecutorController {

    @Autowired
    JobService jobService;

    @Autowired
    LogService logService;


    @ApiOperation(value = "启动定时任务", notes = "启动定时任务")
    @PostMapping("/exec")
    @ResponseBody
    public ResponseEntity executeTask(@RequestBody ExecuteRequest req) throws IOException {
        jobService.exec(req);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "kill任务", notes = "kill任务")
    @PostMapping("/kill")
    @ResponseBody
    public ResponseEntity killTask(@RequestBody KillTaskRequest req) throws IOException {
        jobService.killTask(req);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "任务日志查询", notes = "任务日志查询")
    @GetMapping("/query/logs")
    public void queryLogs(@RequestParam("flowTaskId") Integer flowTaskId,
                          @RequestParam("taskId") Integer taskId,
                          @RequestParam("shardStatus") Integer shardStatus,
                          HttpServletResponse response) throws IOException {

        logService.queryLog(flowTaskId, taskId, shardStatus, response);
    }
}
