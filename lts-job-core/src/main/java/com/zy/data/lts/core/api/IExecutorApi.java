package com.zy.data.lts.core.api;

import com.zy.data.lts.core.model.ExecuteRequest;
import com.zy.data.lts.core.model.KillTaskRequest;
import feign.Headers;
import feign.RequestLine;

/**
 * @author chenqingsong
 * @date 2019/4/1 12:29
 */

public interface IExecutorApi {
    @RequestLine("POST /executor/exec")
    @Headers("Content-Type: application/json")
    void execute(ExecuteRequest request);

    @RequestLine("POST /executor/kill")
    @Headers("Content-Type: application/json")
    void kill(KillTaskRequest request);
}
