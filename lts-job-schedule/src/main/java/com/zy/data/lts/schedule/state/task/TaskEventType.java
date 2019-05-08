package com.zy.data.lts.schedule.state.task;

/**
 * @author chenqingsong
 * @date 2019/4/2 12:18
 */
public enum TaskEventType {
    /**
     * 提交作业，入队准备
     */
    Submit,

    /**
     * 前置任务完成，作业如队列
     */
    Pend,

    /**
     * Kill 作业
     */
    Kill,

    /**
     * 发送task 到executor
     */
    Send,

    /**
     * executor 执行作业
     */
    Execute,

    /**
     * 作业失败
     */
    Fail,

    Finish
}
