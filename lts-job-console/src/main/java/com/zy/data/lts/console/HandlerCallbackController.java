package com.zy.data.lts.console;

import com.zy.data.lts.core.model.JobResultRequest;
import com.zy.data.lts.schedule.service.JobService;
import com.zy.data.lts.schedule.state.flow.FlowEvent;
import com.zy.data.lts.schedule.state.flow.FlowEventType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author chenqingsong
 * @date 2019/4/4 11:14
 */
@Api(description = "Executor作业回调接口，处理任务状态")
@RestController
@RequestMapping("/handler/callback")
public class HandlerCallbackController {

    @Autowired
    JobService jobService;

    @ApiOperation(value = "作业执行成功", notes = "作业执行成功")
    @PostMapping("/success")
    @ResponseBody
    public ResponseEntity successTask(@RequestBody JobResultRequest request) {
        jobService.handleFlowTask(new FlowEvent(request.getFlowTaskId(), FlowEventType.Finish, request.getTaskId(), request.getShard()));
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "作业执行失败", notes = "作业执行失败")
    @PostMapping("/fail")
    @ResponseBody
    public ResponseEntity failTask(@RequestBody JobResultRequest request) {
        jobService.handleFlowTask(new FlowEvent(request.getFlowTaskId(), FlowEventType.Fail, request.getTaskId(), request.getShard()));
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "作业开始执行", notes = "作业开始执行")
    @PostMapping("/start")
    @ResponseBody
    public ResponseEntity startTask(@RequestBody JobResultRequest request) {
        jobService.handleFlowTask(new FlowEvent(request.getFlowTaskId(), FlowEventType.Execute, request.getTaskId(), request.getShard()));
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "Executor关闭自动Kill未完成的作业", notes = "Executor关闭自动Kill未完成的作业")
    @PostMapping("/kill")
    @ResponseBody
    public ResponseEntity killTask(@RequestBody JobResultRequest request) {
        jobService.handleFlowTask(new FlowEvent(request.getFlowTaskId(), FlowEventType.Kill, request.getTaskId(), request.getShard()));
        return ResponseEntity.ok().build();
    }
}
