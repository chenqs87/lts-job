package com.zy.data.lts.console;

import com.github.pagehelper.PageInfo;
import com.zy.data.lts.core.LtsPermitEnum;
import com.zy.data.lts.core.entity.Group;
import com.zy.data.lts.core.entity.User;
import com.zy.data.lts.core.model.PagerRequest;
import com.zy.data.lts.core.tool.SpringContext;
import com.zy.data.lts.model.PermitRequest;
import com.zy.data.lts.naming.config.HandlerConfig;
import com.zy.data.lts.naming.master.IMasterManager;
import com.zy.data.lts.schedule.service.JobService;
import com.zy.data.lts.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.HashMap;
import java.util.Map;

/**
 * @author chenqingsong
 * @date 2019/5/15 16:57
 */
@Api(description = "管理接口")
@RestController
@RequestMapping("/console/manager")
public class ConsoleManagerController {

    @Autowired
    private UserService userService;

    @Autowired
    private JobService jobService;

    @Autowired
    private HandlerConfig handlerConfig;

    @ApiOperation(value = "查询所有用户", notes = "查询所有用户")
    @GetMapping("/getAllUsers")
    public ResponseEntity getAllUsers(PagerRequest request) {
        return ResponseEntity.ok(new PageInfo<>(userService.getAllUsers(request)));
    }

    @ApiOperation(value = "查询所有用户", notes = "查询所有用户")
    @GetMapping("/getUsers")
    public ResponseEntity getUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @ApiOperation(value = "查询所有用户", notes = "查询所有用户")
    @GetMapping("/getGroups")
    public ResponseEntity getGroups() {
        return ResponseEntity.ok(userService.getAllGroups());
    }

    @ApiOperation(value = "查询所有用户组", notes = "查询所有用户组")
    @GetMapping("/getAllGroups")
    public ResponseEntity getAllGroups(PagerRequest request) {
        return ResponseEntity.ok(new PageInfo<>(userService.getAllGroups(request)));
    }

    @ApiOperation(value = "新增用户", notes = "新增用户")
    @PutMapping("/addUser")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity addUser(@RequestBody User user) {
        userService.insert(user);
        return ResponseEntity.ok(user);
    }

    @ApiOperation(value = "新增用户组", notes = "新增用户组")
    @PutMapping("/addGroup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity addGroup(@RequestBody Group group) {
        userService.insert(group);
        return ResponseEntity.ok(group);
    }

    @ApiOperation(value = "新增用户", notes = "新增用户")
    @PostMapping("/updateUser")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity updateUser(@RequestBody User user) {
        userService.update(user);
        return ResponseEntity.ok(user);
    }

    @ApiOperation(value = "新增用户", notes = "新增用户")
    @PostMapping("/updateGroup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity updateGroup(@RequestBody Group group) {
        userService.insert(group);
        return ResponseEntity.ok(group);
    }

    @ApiOperation(value = "删除用户组", notes = "删除用户组")
    @DeleteMapping("/deleteGroup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity deleteGroup(@RequestParam("groupName") String groupName) {
        userService.deleteGroup(groupName);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "删除用户", notes = "删除用户")
    @DeleteMapping("/deleteUser")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity deleteUser(@RequestParam("userName") String userName) {
        userService.deleteUser(userName);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "查询Job权限解析规则", notes = "查询Job权限解析规则")
    @GetMapping("/getJobPermit")
    public ResponseEntity getJobPermit() {
        return ResponseEntity.ok(LtsPermitEnum.getJobPermits());
    }

    @ApiOperation(value = "查询Flow权限解析规则", notes = "查询Flow权限解析规则")
    @GetMapping("/getFlowPermit")
    public ResponseEntity getFlowPermit() {
        return ResponseEntity.ok(LtsPermitEnum.getFlowPermits());
    }

    @ApiOperation(value = "查询资源权限", notes = "查询资源权限")
    @GetMapping("/getResourcePermit")
    public ResponseEntity getResourcePermit(PermitRequest request) {
        return ResponseEntity.ok(userService.getResourcePermit(request));
    }

    @ApiOperation(value = "更新资源权限", notes = "更新资源权限")
    @PostMapping("/updatePermit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity updatePermit(@RequestBody PermitRequest request) {
        userService.updatePermit(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/tasks/info")
    @ApiOperation(value = "查询Master的数量", notes = "查询Master的数量")
    public Map<String, Object> taskInfo() {
        Map<String, Object> ret = new HashMap<>();
        ret.put("masters", handlerConfig.getMasters());
        ret.put("runningTasks", jobService.getRunningTaskSize());
        ret.put("countTasksByDay", jobService.countTasksByDay());
        return ret;
    }
}
