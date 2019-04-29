package com.zy.data.lts.executor.controller;

import com.zy.data.lts.core.model.ExecuteRequest;
import com.zy.data.lts.executor.service.JobService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @ApiOperation(value = "启动定时任务", notes="启动定时任务")
    @PostMapping("/exec")
    @ResponseBody
    public ResponseEntity executeTask(@RequestBody ExecuteRequest req) throws IOException {
        jobService.exec(req);
        return ResponseEntity.ok().build();
    }
}
