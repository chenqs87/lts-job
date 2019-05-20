package com.zy.data.lts.schedule.alert;

import com.zy.data.lts.core.entity.FlowTask;

/**
 * @author chenqingsong
 * @date 2019/5/20 14:10
 */
public interface IAlerter {
    void success(FlowTask task);
    void failed(FlowTask task);
}
