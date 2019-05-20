package com.zy.data.lts.schedule.alert;

import com.zy.data.lts.core.entity.FlowTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author chenqingsong
 * @date 2019/5/20 14:12
 */
@Component
public class PhoneAlerter implements IAlerter {

    private static Logger logger = LoggerFactory.getLogger(PhoneAlerter.class);

    @Override
    public void success(FlowTask message) {
        logger.info("Success :::", message);
    }

    @Override
    public void failed(FlowTask error) {
        logger.warn("Fail :::", error);
    }
}
