package com.zy.data.lts.schedule.timer;

import com.zy.data.lts.core.TriggerFlowEvent;
import com.zy.data.lts.core.TriggerMode;
import com.zy.data.lts.core.tool.SpringContext;
import com.zy.data.lts.schedule.trigger.JobTrigger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * @author chenqingsong
 * @date 2019/3/28 11:28
 */
@DisallowConcurrentExecution
public class LtsScheduleJob extends QuartzJobBean {
    @Override
    protected void executeInternal(JobExecutionContext context) {
        JobKey jobKey = context.getJobDetail().getKey();
        String jobName = jobKey.getName();
        int flowId = Integer.parseInt(jobName);
        //JobTrigger.triggerCronFlow(flowId);
        SpringContext.publishEvent(new TriggerFlowEvent(flowId, TriggerMode.Cron, null));
    }
}
