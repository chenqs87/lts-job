package com.zy.data.lts.executor.model;

import java.nio.file.Path;

/**
 * @author chenqingsong
 * @date 2019/4/9 20:17
 */
public class PythonEvent extends JobExecuteEvent {

    public PythonEvent() {
        super();
    }

    public PythonEvent(int flowTaskId, int taskId, int shard, Path output, String params) {
        super(flowTaskId, taskId, shard, output, params);
    }
}
