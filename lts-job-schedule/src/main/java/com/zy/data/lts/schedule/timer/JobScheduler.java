package com.zy.data.lts.schedule.timer;

import com.zy.data.lts.schedule.trigger.JobTrigger;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Date;

/**
 * @author chenqingsong
 * @date 2019/3/28 11:08
 */

@Component
public class JobScheduler {
    private static final Logger logger = LoggerFactory.getLogger(JobScheduler.class);

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private JobTrigger jobTrigger;

    @PostConstruct
    public void init() throws SchedulerException {
        scheduler.start();
    }

    @PreDestroy
    public void destroy() throws SchedulerException {
        scheduler.shutdown();
    }

    /**
     * 启动定时任务
     * @param jobName 作业名称
     * @param cronExpression cron 表达式
     * @throws Exception
     */
    public  boolean startJob(String jobName, String cronExpression) throws Exception {

        TriggerKey triggerKey = TriggerKey.triggerKey(jobName);
        JobKey jobKey = new JobKey(jobName);

        if (scheduler.checkExists(triggerKey)) {
            return true;
        }

        CronTrigger cronTrigger = createCronTrigger(cronExpression, triggerKey);
        JobDetail jobDetail = JobBuilder.newJob(LtsScheduleJob.class).withIdentity(jobKey).build();

        Date date = scheduler.scheduleJob(jobDetail, cronTrigger);
        logger.info("Begin to start a cron job. time: {}", date);
        return true;
    }

    public  void stopJob(String jobName) throws SchedulerException {
        scheduler.deleteJob(new JobKey(jobName));
    }


    private CronTrigger createCronTrigger(String cronExpression, TriggerKey triggerKey) {
        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder
                .cronSchedule(cronExpression)
                .withMisfireHandlingInstructionDoNothing();
        return TriggerBuilder
                .newTrigger()
                .withIdentity(triggerKey)
                .withSchedule(cronScheduleBuilder)
                .build();
    }

}
