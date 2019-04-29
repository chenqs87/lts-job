package com.zy.data.lts.console;

import com.zy.data.lts.core.api.ExecutorApi;
import com.zy.data.lts.core.model.BeatInfoRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @author chenqingsong
 * @date 2019/4/4 11:14
 */
@Api(description = "Executor状态接口")
@RestController
@RequestMapping("/handler/status")
public class HandlerStatusController {

    @Autowired
    ExecutorApi executorApi;

    @ApiOperation(value = "Handler心跳" ,  notes="Handler心跳")
    @PostMapping("/beat")
    @ResponseBody
    public ResponseEntity beat(HttpServletRequest request, @RequestBody BeatInfoRequest beatInfo) {
        String host = request.getRemoteAddr();
        beatInfo.setHost(host);
        executorApi.refresh(beatInfo);

        return ResponseEntity.ok().build();
    }
}
