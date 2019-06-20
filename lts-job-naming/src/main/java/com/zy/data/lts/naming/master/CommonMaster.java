package com.zy.data.lts.naming.master;

import com.zy.data.lts.core.TriggerFlowEvent;
import com.zy.data.lts.core.TriggerMode;
import com.zy.data.lts.core.api.IAdminApi;
import com.zy.data.lts.core.model.BeatInfoRequest;
import com.zy.data.lts.core.model.JobResultRequest;
import com.zy.data.lts.core.tool.SpringContext;
import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;


public class CommonMaster implements IMaster {

    private String host;

    private IAdminApi adminApi;

    public CommonMaster(String host) {
        this.host = host;
        adminApi = Feign.builder()
                .encoder(new GsonEncoder())
                .decoder(new GsonDecoder())
                .target(IAdminApi.class, "http://" + host);
    }

    @Override
    public void trigger(int flowId, TriggerMode triggerMode, String params) {
        SpringContext.publishEvent(new TriggerFlowEvent(flowId, triggerMode, params));
    }

    @Override
    public void success(JobResultRequest request) {
        adminApi.success(request);
    }

    @Override
    public void fail(JobResultRequest request) {
        adminApi.fail(request);
    }

    @Override
    public void start(JobResultRequest request) {
        adminApi.start(request);
    }

    @Override
    public void kill(JobResultRequest request) {
        adminApi.kill(request);
    }

    @Override
    public void beat(BeatInfoRequest request) {
        adminApi.beat(request);
    }

    public String getHost() {
        return host;
    }
}
