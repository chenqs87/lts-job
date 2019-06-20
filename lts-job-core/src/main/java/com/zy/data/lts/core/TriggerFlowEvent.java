package com.zy.data.lts.core;

public class TriggerFlowEvent {
    private int flowId;
    private TriggerMode triggerMode;
    private String params;

    public TriggerFlowEvent(int flowId, TriggerMode triggerMode, String params) {
        this.flowId = flowId;
        this.triggerMode = triggerMode;
        this.params = params;
    }

    public int getFlowId() {
        return flowId;
    }

    public TriggerMode getTriggerMode() {
        return triggerMode;
    }

    public String getParams() {
        return params;
    }
}
