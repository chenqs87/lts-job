package com.zy.data.lts.core.api;


import com.zy.data.lts.core.model.BeatInfoRequest;
import com.zy.data.lts.core.model.JobResultRequest;
import feign.Headers;
import feign.RequestLine;

/**
 * @author chenqingsong
 * @date 2019/4/1 12:29
 */
public interface IMaster {

    @RequestLine("POST /handler/callback/success")
    @Headers("Content-Type: application/json")
    void success(JobResultRequest request);

    @RequestLine("POST /handler/callback/fail")
    @Headers("Content-Type: application/json")
    void fail(JobResultRequest request);

    @RequestLine("POST /handler/callback/start")
    @Headers("Content-Type: application/json")
    void start(JobResultRequest request);

    @RequestLine("POST /handler/callback/kill")
    @Headers("Content-Type: application/json")
    void kill(JobResultRequest request);

    /**
     * 心跳API
     */
    @RequestLine("POST /handler/status/beat")
    @Headers("Content-Type: application/json")
    void beat(BeatInfoRequest request);

}
