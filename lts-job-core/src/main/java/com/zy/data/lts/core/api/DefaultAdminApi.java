package com.zy.data.lts.core.api;

import com.zy.data.lts.core.model.BeatInfoRequest;
import com.zy.data.lts.core.model.JobResultRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 默认AdminApi 当Executor退出，并且Master不可用时，使用
 *
 * @author chenqingsong
 * @date 2019/5/8 16:43
 */
@Component
public class DefaultAdminApi implements IAdminApi {

    private final static Logger logger = LoggerFactory.getLogger(DefaultAdminApi.class);

    @Override
    public void success(JobResultRequest request) {
        logger.warn("Fail to execute success, Msg: {}", request);
    }

    @Override
    public void fail(JobResultRequest request) {
        logger.warn("Fail to execute fail, Msg: {}", request);
    }

    @Override
    public void start(JobResultRequest request) {
        logger.warn("Fail to execute start, Msg: {}", request);
    }

    @Override
    public void kill(JobResultRequest request) {
        logger.warn("Fail to execute kill, Msg: {}", request);
    }

    @Override
    public void beat(BeatInfoRequest request) {

    }
}
