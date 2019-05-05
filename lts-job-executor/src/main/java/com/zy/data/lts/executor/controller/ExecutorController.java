package com.zy.data.lts.executor.controller;

import com.zy.data.lts.core.model.ExecuteRequest;
import com.zy.data.lts.core.model.LogQueryRequest;
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


    @ApiOperation(value = "启动定时任务", notes="启动定时任务")
    @PostMapping("/exec")
    @ResponseBody
    public ResponseEntity executeTask(@RequestBody ExecuteRequest req) throws IOException {
        jobService.exec(req);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "任务日志查询", notes="任务日志查询")
    @GetMapping("/query/logs")
    public void queryLogs(@RequestParam("flowTaskId") Integer flowTaskId,
                          @RequestParam("taskId") Integer taskId,
                          @RequestParam("shardStatus") Integer shardStatus,
                          HttpServletResponse response) throws IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
        response.setHeader("Access-Control-Allow-Headers", "content-type");
        response.setHeader("Cache-Control","no store");
        response.setHeader("Pragma","no store");
        response.setDateHeader("Expires",0);

        logService.queryLog(flowTaskId, taskId, shardStatus, response);
    }
}
