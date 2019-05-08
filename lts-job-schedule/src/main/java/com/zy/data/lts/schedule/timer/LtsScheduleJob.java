package com.zy.data.lts.schedule.timer;

import com.zy.data.lts.schedule.trigger.JobTrigger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * @author chenqingsong
 * @date 2019/3/28 11:28
 */
public class LtsScheduleJob extends QuartzJobBean {
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        JobKey jobKey = context.getJobDetail().getKey();
        String jobName = jobKey.getName();
        int flowId = Integer.parseInt(jobName);
        JobTrigger.pushCronFlow(flowId);
    }
}
