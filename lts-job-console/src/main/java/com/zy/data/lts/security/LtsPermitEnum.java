package com.zy.data.lts.security;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static com.zy.data.lts.security.LtsPermitType.Flow;
import static com.zy.data.lts.security.LtsPermitType.Job;

/**
 * @author chenqingsong
 * @date 2019/5/14 15:44
 */
public enum LtsPermitEnum {
    // Job相关任务
    JobView(Job, 0),
    JobEdit(Job, 1),
    JobDelete(Job, 2),
    JobAuthorized(Job, 3),

    //Flow 相关任务
    FlowView(Flow, 0),
    FlowCron(Flow, 1),
    FlowEdit(Flow, 2),
    FlowDelete(Flow, 3),
    FlowExec(Flow, 4),
    FlowAuthorized(Flow, 5);

    private final static Map<String, Integer> jobPermit = new HashMap<>();
    private final static Map<String, Integer> flowPermit = new HashMap<>();

    static {
        Stream.of(LtsPermitEnum.values()).forEach(lp -> {
            switch (lp.type) {
                case Flow: flowPermit.put(lp.name(), lp.code); break;
                case Job: jobPermit.putIfAbsent(lp.name(), lp.code); break;
            }
        });
    }
    public int code;
    public LtsPermitType type;
    LtsPermitEnum(LtsPermitType type, int code) {
        this.type = type;
        this.code = 1 << code;
    }

    public static Map<String, Integer> getJobPermits() {
        return Collections.synchronizedMap(jobPermit);
    }

    public static Map<String, Integer> getFlowPermits() {
        return Collections.synchronizedMap(flowPermit);
    }

    public static int getAllJobPermit() {
        int permitAll = 0;
        for (Integer permit : jobPermit.values()) {
            permitAll |= permit;
        }
        return permitAll;
    }

    public static int getAllFlowPermit() {
        int permitAll = 0;
        for (Integer permit : flowPermit.values()) {
            permitAll |= permit;
        }
        return permitAll;
    }
}
