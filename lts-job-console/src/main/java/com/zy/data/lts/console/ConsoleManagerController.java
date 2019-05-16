package com.zy.data.lts.console;

import com.github.pagehelper.PageInfo;
import com.zy.data.lts.core.entity.Group;
import com.zy.data.lts.core.entity.User;
import com.zy.data.lts.core.model.PagerRequest;
import com.zy.data.lts.security.LtsPermitEnum;
import com.zy.data.lts.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @ApiOperation(value = "查询所有用户组", notes = "查询所有用户组")
    @GetMapping("/getAllGroups")
    public ResponseEntity getAllGroups(PagerRequest request) {
        return ResponseEntity.ok(new PageInfo<>(userService.getAllGroups(request)));
    }

    @ApiOperation(value = "新增用户", notes = "新增用户")
    @PutMapping("/addUser")
    public ResponseEntity addUser(@RequestBody User user) {
        userService.insert(user);
        return ResponseEntity.ok(user);
    }

    @ApiOperation(value = "新增用户组", notes = "新增用户组")
    @PutMapping("/addGroup")
    public ResponseEntity addGroup(@RequestBody Group group) {
        userService.insert(group);
        return ResponseEntity.ok(group);
    }

    @ApiOperation(value = "新增用户", notes = "新增用户")
    @PostMapping("/updateUser")
    public ResponseEntity updateUser(@RequestBody User user) {
        userService.update(user);
        return ResponseEntity.ok(user);
    }

    @ApiOperation(value = "新增用户", notes = "新增用户")
    @PostMapping("/updateGroup")
    public ResponseEntity updateGroup(@RequestBody Group group) {
        userService.insert(group);
        return ResponseEntity.ok(group);
    }

    @ApiOperation(value = "删除用户组", notes = "删除用户组")
    @DeleteMapping("/deleteGroup")
    public ResponseEntity deleteGroup(@RequestParam("groupName") String groupName) {
        userService.deleteGroup(groupName);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "删除用户", notes = "删除用户")
    @DeleteMapping("/deleteUser")
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
}
