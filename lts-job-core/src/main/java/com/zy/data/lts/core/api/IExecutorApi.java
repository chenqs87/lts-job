package com.zy.data.lts.core.api;

import com.zy.data.lts.core.model.ExecuteRequest;
import com.zy.data.lts.core.model.LogQueryRequest;
import feign.Headers;
import feign.RequestLine;

import java.util.List;

/**
 * @author chenqingsong
 * @date 2019/4/1 12:29
 */

public interface IExecutorApi {
    @RequestLine("POST /executor/exec")
    @Headers("Content-Type: application/json")
    void execute(ExecuteRequest request);
}
