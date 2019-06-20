package com.zy.data.lts.naming.master;

import com.zy.data.lts.core.TriggerMode;
import com.zy.data.lts.core.model.BeatInfoRequest;
import com.zy.data.lts.core.model.JobResultRequest;
import org.springframework.context.ApplicationListener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 高可用，负载平衡
 */
public class ShardindLoadMaster implements IMaster, ApplicationListener<LtsMasterChangeEvent> {

    private Map<String, IMaster> masters = new ConcurrentHashMap<>();

    @Override
    public void success(JobResultRequest request) {

    }

    @Override
    public void fail(JobResultRequest request) {

    }

    @Override
    public void start(JobResultRequest request) {

    }

    @Override
    public void kill(JobResultRequest request) {

    }

    @Override
    public void beat(BeatInfoRequest request) {

    }

    @Override
    public void onApplicationEvent(LtsMasterChangeEvent event) {

        switch (event.getEventType()) {
            case NEW: break;
            case DELETE: break;
        }
    }


    @Override
    public void trigger(int flowId, TriggerMode triggerMode, String params) {

    }
}
