package com.zy.data.lts.core;

/**
 * @author chenqingsong
 * @date 2019/3/28 17:51
 */
public enum TaskStatus {

    New(0),
    Ready(1),
    Pending(2),
    Submitted(3),
    Running(4),
    Failed(5),
    Finished(6),
    Killed(7);

    int code;

    TaskStatus(int code) {
        this.code = code;
    }

    public static TaskStatus parse(int code) {
        switch (code) {
            case 0:
                return New;
            case 1:
                return Ready;
            case 2:
                return Pending;
            case 3:
                return Submitted;
            case 4:
                return Running;
            case 5:
                return Failed;
            case 6:
                return Finished;
            case 7:
                return Killed;
            default:
                throw new IllegalArgumentException("Fail to parse code to TaskStatus!");
        }
    }

    public boolean isFinish() {
        switch (code) {
            case 5:
            case 6:
            case 7:
                return true;
            default:
                return false;
        }
    }

    public int code() {
        return code;
    }
}
