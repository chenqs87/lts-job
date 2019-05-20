package com.zy.data.lts.schedule.alert;

import com.zy.data.lts.core.entity.FlowTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author chenqingsong
 * @date 2019/5/20 14:15
 */
@Component
public class CommonAlerter implements IAlerter {

    @Autowired
    private PhoneAlerter alerter;

    @Override
    public void success(FlowTask message) {
        alerter.success(message);
    }

    @Override
    public void failed(FlowTask error) {
        alerter.failed(error);
    }
}
