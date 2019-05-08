package com.zy.data.lts.schedule.state.flow;

/**
 * @author chenqingsong
 * @date 2019/3/29 12:25
 */
public enum FlowTaskStatus {
    New(0),
    Pending(1),
    Running(2),
    Killed(3),
    Finished(4),
    Failed(5);

    int code;

    FlowTaskStatus(int code) {
        this.code = code;
    }

    public static FlowTaskStatus parse(int code) {
        switch (code) {
            case 0:
                return New;
            case 1:
                return Pending;
            case 2:
                return Running;
            case 3:
                return Killed;
            case 4:
                return Finished;
            case 5:
                return Failed;
            default:
                throw new IllegalArgumentException("FlowTaskStatus Code is wrong!!!");
        }
    }

    public int code() {
        return code;
    }
}
