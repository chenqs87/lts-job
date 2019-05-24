package com.zy.data.lts.executor.type;

import com.zy.data.lts.executor.model.JobExecuteEvent;
import org.springframework.stereotype.Component;

/**
 *
 * @author chenqingsong
 * @date 2019/5/23 11:13
 */
public interface IJobTypeHandler {
    String[] createCommand(JobExecuteEvent event);

    default String getScriptName() {
        return "exec";
    }
}
