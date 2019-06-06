package com.zy.data.lts.console;

import com.github.pagehelper.PageInfo;
import com.zy.data.lts.core.FlowTaskStatus;
import com.zy.data.lts.core.LtsPermitEnum;
import com.zy.data.lts.core.LtsPermitType;
import com.zy.data.lts.core.RoleEnum;
import com.zy.data.lts.core.TriggerMode;
import com.zy.data.lts.core.entity.AlertConfig;
import com.zy.data.lts.core.entity.ImportDataFlow;
import com.zy.data.lts.core.entity.Job;
import com.zy.data.lts.core.model.FlowQueryRequest;
import com.zy.data.lts.core.model.JobQueryRequest;
import com.zy.data.lts.schedule.handler.HandlerService;
import com.zy.data.lts.schedule.service.JobService;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.servlet.http.HttpServletResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

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
    HandlerService handlerService;

    @ApiOperation(value = "启动定时任务", notes = "启动定时任务")
    @PostMapping("/cronFlow")
    @PreAuthorize("hasPermission(#flowId, 'FlowCron')")
    public ResponseEntity startCronFlow(@RequestParam("flowId") Integer flowId) throws Exception {
        return ResponseEntity.ok(jobService.startCronFlow(flowId));
    }

    @ApiOperation(value = "停止定时任务", notes = "停止定时任务")
    @DeleteMapping("/cronFlow")
    @PreAuthorize("hasPermission(#flowId, 'FlowCron')")
    public ResponseEntity stopCronFlow(@RequestParam("flowId") Integer flowId) throws Exception {
        return ResponseEntity.ok(jobService.stopCronFlow(flowId));
    }

    @ApiOperation(value = "新建工作流", notes = "新建工作流")
    @PutMapping("/flow")
    public ResponseEntity createFlow(@RequestBody AlertConfig flow) {
        flow.setCreateUser(getCurrentUserName());
        flow.setPermit(LtsPermitEnum.getAllFlowPermit());
        flow.setType(LtsPermitType.Flow.name());
        return ResponseEntity.ok(jobService.createFlow(flow));
    }

    @ApiOperation(value = "新建数据导入工作流", notes = "新建数据导入工作流")
    @PostMapping("/importDataFlow")
    public ResponseEntity createImportDataFlow(
            @RequestBody ImportDataFlow importDataFlow) {
        return ResponseEntity.ok(jobService.createImportDataFlow(getCurrentUserName(),importDataFlow));
    }

    @ApiOperation(value = "删除工作流", notes = "删除工作流")
    @DeleteMapping("/flow")
    @PreAuthorize("hasPermission(#flowId, 'FlowDelete')")
    public ResponseEntity deleteFlow(@RequestParam("flowId") Integer flowId) {
        jobService.deleteFlow(flowId);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "更新工作流", notes = "更新工作流")
    @PostMapping("/flow")
    @PreAuthorize("hasPermission(#flow.id, 'FlowEdit')")
    public ResponseEntity updateFlow(@RequestBody AlertConfig flow) {
        return ResponseEntity.ok(jobService.updateFlow(flow));
    }

    @ApiOperation(value = "新建任务", notes = "新建任务")
    @PutMapping("/job")
    public ResponseEntity createJob(@RequestBody Job job) {
        job.setCreateUser(getCurrentUserName());
        job.setPermit(LtsPermitEnum.getAllJobPermit());
        job.setType(LtsPermitType.Job.name());
        return ResponseEntity.ok(jobService.createJob(job));
    }

    @ApiOperation(value = "删除任务", notes = "删除任务")
    @DeleteMapping("/job")
    @PreAuthorize("hasPermission(#jobId, 'JobDelete')")
    public ResponseEntity deleteJob(@RequestParam("jobId") Integer jobId) {
        jobService.deleteJob(jobId);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "更新任务", notes = "更新任务")
    @PostMapping("/job")
    @PreAuthorize("hasPermission(#job.id, 'JobEdit')")
    public ResponseEntity updateJob(@RequestBody Job job) {
        return ResponseEntity.ok(jobService.updateJob(job));
    }

    @ApiOperation(value = "执行工作流", notes = "执行工作流")
    @PostMapping("/triggerFlow")
    @PreAuthorize("hasPermission(#flowId, 'FlowExec')")
    public ResponseEntity triggerFlow(
            @RequestParam("flowId") Integer flowId,
            @RequestParam("params") String params) {
        jobService.triggerFlow(flowId, TriggerMode.Click, params);
        return ResponseEntity.ok().build();
    }


    @ApiOperation(value = "重新执行工作流", notes = "重新执行工作流")
    @PostMapping("/reTriggerFlow")
    @PreAuthorize("hasPermission(#flowId, 'FlowExec')")
    public ResponseEntity reTriggerFlow(
            @RequestParam("flowId") Integer flowId,
            @RequestParam("flowTaskId") Integer flowTaskId,
            @RequestParam("params") String params) {
        jobService.reTriggerFlow(flowTaskId, params);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "根据用户name查询所有任务", notes = "查询所有任务")
    @GetMapping("/getAllJobsByUser")
    public ResponseEntity getAllJobsByUser(JobQueryRequest request) {
        request.setUsername(getCurrentUserName());
        request.setPermit(LtsPermitEnum.JobView.code);

        return ResponseEntity.ok(new PageInfo<>(isAdmin() ?
                jobService.findAllJobs(request) : jobService.findJobsByUser(request)));
    }

    @ApiOperation(value = "根据用户group查询所有任务", notes = "查询所有任务")
    @GetMapping("/getAllJobsByGroup")
    public ResponseEntity getAllJobsByGroup(JobQueryRequest request) {
        request.setUsername(getCurrentUserName());
        request.setPermit(LtsPermitEnum.JobView.code);
        return ResponseEntity.ok(new PageInfo<>(isAdmin() ?
                jobService.findAllJobs(request) : jobService.findJobsByGroup(request)));
    }

    @ApiOperation(value = "根据用户name查询所有工作流", notes = "查询所有工作流")
    @GetMapping("/getAllFlowsByUser")
    public ResponseEntity getAllFlowsByUser(FlowQueryRequest request) {
        request.setUsername(getCurrentUserName());
        request.setPermit(LtsPermitEnum.FlowView.code);
        return ResponseEntity.ok(new PageInfo<>(isAdmin() ?
                jobService.findAllFlows(request) : jobService.findFlowsByUser(request)));
    }

    @ApiOperation(value = "根据用户group查询所有工作流", notes = "查询所有工作流")
    @GetMapping("/getAllFlowsByGroup")
    public ResponseEntity getAllFlowsByGroup(FlowQueryRequest request) {
        request.setUsername(getCurrentUserName());
        request.setPermit(LtsPermitEnum.FlowView.code);
        return ResponseEntity.ok(new PageInfo<>(isAdmin() ?
                jobService.findAllFlows(request) : jobService.findFlowsByGroup(request)));
    }

    @ApiOperation(value = "查询工作流", notes = "查询工作流")
    @GetMapping("/getFlow")
    @PreAuthorize("hasPermission(#flowId, 'FlowView')")
    public ResponseEntity getFlow(@RequestParam("flowId") Integer flowId) {
        return ResponseEntity.ok(jobService.getFlowById(flowId));
    }

    @ApiOperation(value = "查询所有工作流任务", notes = "查询所有工作流任务")
    @GetMapping("/getFlowTasks")
    public ResponseEntity getFlowTasks(
            @RequestParam("flowId") Integer flowId,
            @RequestParam("status") Integer status,
            @RequestParam("pageNum") Integer pageNum,
            @RequestParam("pageSize") Integer pageSize) {
        return ResponseEntity.ok(new PageInfo<>(jobService.findFlowTask(flowId, status, pageNum, pageSize)));
    }

    @ApiOperation(value = "查询所有任务", notes = "查询所有任务")
    @GetMapping("/getTasks")
    public ResponseEntity getTasks(
            @RequestParam("flowTaskId") Integer flowTaskId,
            @RequestParam("pageNum") Integer pageNum,
            @RequestParam("pageSize") Integer pageSize) {
        return ResponseEntity.ok(new PageInfo<>(jobService.findTaskByFlowTaskId(flowTaskId, pageNum, pageSize)));
    }

    @ApiOperation(value = "kill任务", notes = "kill任务")
    @PostMapping("/killFlowTask")
    @PreAuthorize("hasPermission(#flowId, 'FlowDelete')")
    public ResponseEntity killFlowTask(
            @RequestParam("flowId") Integer flowId,
            @RequestParam("flowTaskId") Integer flowTaskId) {
        jobService.killFlowTask(flowTaskId);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "查询所有handler", notes = "查询所有handler")
    @GetMapping("/getHandlers")
    public ResponseEntity getHandlers() {
        return ResponseEntity.ok(handlerService.getActiveExecutors());
    }

    @ApiOperation(value = "任务日志查询", notes = "任务日志查询")
    @GetMapping("/query/logs")
    public void queryLogs(
            @RequestParam("flowTaskId") Integer flowTaskId,
            @RequestParam("taskId") Integer taskId,
            @RequestParam("shardStatus") Integer shardStatus,
            @RequestParam("logName") String logName,
            @RequestParam("host") String host,
            HttpServletResponse response) throws IOException {

        URL url = new URL("http://" + host + "/executor/query/logs?flowTaskId=" + flowTaskId +
                "&taskId=" + taskId + "&shardStatus=" + shardStatus + "&logName=" + logName);

        try (InputStream inputStream = url.openStream()) {
            int count = IOUtils.copy(inputStream, response.getOutputStream());
            response.setHeader("FileSize", String.valueOf(count));
        }
    }

    @ApiOperation(value = "查询指定flow的报警配置", notes = "查询指定flow的报警配置")
    @GetMapping("/getAlertConfig")
    public ResponseEntity getAlertConfig(@RequestParam("flowId") Integer flowId) {
        return ResponseEntity.ok(jobService.getAlertConfig(flowId));
    }



    @ApiOperation(value = "获取FlowTaskStatus", notes = "获取FlowTaskStatus")
    @GetMapping("/getFlowTaskStatus")
    public ResponseEntity getFlowTaskStatus() {
        return ResponseEntity.ok(FlowTaskStatus.values());
    }

    @ApiOperation(value = "获取FlowTaskStatus", notes = "获取FlowTaskStatus")
    @GetMapping("/getFlowScheduleLog")
    public ResponseEntity getFlowScheduleLog(@RequestParam("flowTaskId") Integer flowTaskId) {
        return ResponseEntity.ok(jobService.getFlowScheduleLog(flowTaskId));
    }

    private String getCurrentUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        return user.getUsername();
    }

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(RoleEnum.ROLE_ADMIN.name());
        return user.getAuthorities().contains(authority);
    }
}
