package com.zy.data.lts.console;

import com.github.pagehelper.PageInfo;
import com.zy.data.lts.core.TriggerMode;
import com.zy.data.lts.core.api.ExecutorApi;
import com.zy.data.lts.core.entity.Flow;
import com.zy.data.lts.core.entity.Job;
import com.zy.data.lts.core.model.JobQueryRequest;
import com.zy.data.lts.core.model.PagerRequest;
import com.zy.data.lts.schedule.service.JobService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author chenqingsong
 * @date 2019/4/4 10:25
 */
@Api(description = "工作流接口")
@RestController
@RequestMapping("/console/flow")
public class ConsoleFlowController {

    @Autowired
    JobService jobService;

    @Autowired
    ExecutorApi executorApi;

    @ApiOperation(value = "启动定时任务",  notes="启动定时任务")
    @PostMapping("/cronFlow")
    public ResponseEntity startCronFlow(@RequestParam("flowId") Integer flowId) throws Exception {
        jobService.startCronFlow(flowId);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "停止定时任务",  notes="停止定时任务")
    @DeleteMapping ("/cronFlow")
    public ResponseEntity stopCronFlow(@RequestParam("flowId") Integer flowId) throws Exception {
        jobService.stopCronFlow(flowId);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "新建工作流",  notes="新建工作流")
    @PutMapping ("/flow")
    public ResponseEntity createFlow(@RequestBody Flow flow) {
        return ResponseEntity.ok(jobService.createFlow(flow));
    }

    @ApiOperation(value = "删除工作流",  notes="删除工作流")
    @DeleteMapping ("/flow")
    public ResponseEntity deleteFlow(@RequestParam("flowId") Integer flowId) {
        jobService.deleteFlow(flowId);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "更新工作流",  notes="更新工作流")
    @PostMapping ("/flow")
    public ResponseEntity updateFlow(@RequestBody Flow flow) {
        return ResponseEntity.ok(jobService.updateFlow(flow));
    }


    @ApiOperation(value = "新建任务",  notes="新建任务")
    @PutMapping ("/job")
    public ResponseEntity createJob(@RequestBody Job job) {
        return ResponseEntity.ok(jobService.createJob(job));
    }

    @ApiOperation(value = "删除任务",  notes="删除任务")
    @DeleteMapping ("/job")
    public ResponseEntity deleteJob(@RequestParam("jobId") Integer jobId) {
        jobService.deleteJob(jobId);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "更新任务",  notes="更新任务")
    @PostMapping ("/job")
    public ResponseEntity updateJob(@RequestBody Job job) {
        return ResponseEntity.ok(jobService.updateJob(job));
    }

    @ApiOperation(value = "执行工作流",  notes="执行工作流")
    @PostMapping("/triggerFlow")
    public ResponseEntity triggerFlow(@RequestParam("flowId") Integer flowId,
                                      @RequestParam ("params") String params) {
        jobService.triggerFlow(flowId, TriggerMode.Click, params);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "查询所有任务",  notes="查询所有任务")
    @GetMapping("/getAllJobs")
    public ResponseEntity getAllJobs(JobQueryRequest request) {
        return ResponseEntity.ok(new PageInfo<>(jobService.findAllJobs(request)));
    }

    @ApiOperation(value = "查询所有工作流",  notes="查询所有工作流")
    @GetMapping("/getAllFlows")
    public ResponseEntity getAllFlows(PagerRequest request) {
        return ResponseEntity.ok(new PageInfo<>(jobService.findAllFlows(request.getPageNum(), request.getPageSize())));
    }

    @ApiOperation(value = "查询工作流",  notes="查询工作流")
    @GetMapping("/getFlow")
    public ResponseEntity getFlow(@RequestParam("flowId") Integer flowId) {
        return ResponseEntity.ok(jobService.getFlowById(flowId));
    }

    @ApiOperation(value = "查询所有工作流任务",  notes="查询所有工作流任务")
    @GetMapping("/getAllFlowTasks")
    public ResponseEntity getAllFlowTasks(@RequestParam("pageNum") Integer pageNum,
                                          @RequestParam("pageSize") Integer pageSize) {
        return ResponseEntity.ok(new PageInfo<>(jobService.findAllFlowTask(pageNum, pageSize)));
    }

    @ApiOperation(value = "查询指定工作流任务",  notes="查询指定工作流任务")
    @GetMapping("/getFlowTasksByFlowId")
    public ResponseEntity getFlowTasksByFlowId(@RequestParam("flowId") Integer flowId,
                                               @RequestParam("pageNum") Integer pageNum,
                                               @RequestParam("pageSize") Integer pageSize) {
        return ResponseEntity.ok(new PageInfo<>(jobService.findByFlowId(flowId, pageNum, pageSize)));
    }

    @ApiOperation(value = "查询所有任务",  notes="查询所有任务")
    @GetMapping("/getTasks")
    public ResponseEntity getTasks(@RequestParam("flowTaskId") Integer flowTaskId,
                                   @RequestParam("pageNum") Integer pageNum,
                                   @RequestParam("pageSize") Integer pageSize) {
        return ResponseEntity.ok(new PageInfo<>(jobService.findTaskByFlowTaskId(flowTaskId, pageNum, pageSize)));
    }

    @ApiOperation(value = "kill任务",  notes="kill任务")
    @PostMapping("/killFlowTask")
    public ResponseEntity killFlowTask(@RequestParam("flowTaskId") Integer flowTaskId) {
        jobService.killFlowTask(flowTaskId);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "查询所有handler",  notes="查询所有handler")
    @GetMapping("/getHandlers")
    public ResponseEntity getHandlers() {
        return ResponseEntity.ok(executorApi.getActiveExecutors());
    }

}
