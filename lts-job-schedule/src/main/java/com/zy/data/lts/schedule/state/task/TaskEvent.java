package com.zy.data.lts.schedule.state.task;

import com.zy.data.lts.schedule.state.AbstractEvent;

/**
 * @author chenqingsong
 * @date 2019/4/2 12:17
 */
public class TaskEvent extends AbstractEvent<TaskEventType> {

    private int shardStatus;

    public TaskEvent(TaskEventType taskEventType, int shardStatus) {
        super(taskEventType);
        this.shardStatus = shardStatus;
    }

    public TaskEvent(TaskEventType taskEventType) {
        this(taskEventType, 1);
    }

    public int getShardStatus() {
        return shardStatus;
    }
}
