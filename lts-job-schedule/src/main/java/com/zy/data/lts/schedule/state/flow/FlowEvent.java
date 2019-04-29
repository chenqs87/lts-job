package com.zy.data.lts.schedule.state.flow;

import com.zy.data.lts.schedule.state.AbstractEvent;

/**
 * @author chenqingsong
 * @date 2019/4/2 12:17
 */
public class FlowEvent extends AbstractEvent<FlowEventType> {

    /**
     * 当前处理的currentTaskId, 部分事件会用到
     */
    private int currentTaskId = -1;

    private int currentTaskShard = 1;

    private int flowTaskId = -1;

    public FlowEvent(int flowTaskId, FlowEventType flowEventType, int currentTaskId, int currentTaskShard) {
        super(flowEventType);
        this.flowTaskId = flowTaskId;
        this.currentTaskId = currentTaskId;
        this.currentTaskShard = currentTaskShard;
    }

    public FlowEvent(int flowTaskId, FlowEventType flowEventType) {
        this(flowTaskId, flowEventType, -1, 1);
    }

    public FlowEvent(FlowEventType flowEventType) {
        super(flowEventType);
    }

    public int getCurrentTaskId() {
        return currentTaskId;
    }

    public int getFlowTaskId() {
        return flowTaskId;
    }

    public int getCurrentTaskShard() {
        return currentTaskShard;
    }
}
