package com.zy.data.lts.schedule.timer;

import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.sql.DataSource;

/**
 * @author chenqingsong
 * @date 2019/3/27 20:38
 */

@Configuration
public class SchedulerConfig {

    @Autowired
    private DataSource dataSource;

    @Bean(name = "ltsSchedulerFactoryBean")
    public SchedulerFactoryBean getSchedulerFactoryBean() {

        SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
        schedulerFactory.setDataSource(dataSource);
        schedulerFactory.setAutoStartup(true);
        schedulerFactory.setStartupDelay(20);
        schedulerFactory.setOverwriteExistingJobs(true);
        schedulerFactory.setApplicationContextSchedulerContextKey("applicationContext");
        schedulerFactory.setConfigLocation(new ClassPathResource("quartz.properties"));

        return schedulerFactory;
    }

    @Bean(name = "scheduler")
    public Scheduler scheduler(@Qualifier("ltsSchedulerFactoryBean") SchedulerFactoryBean schedulerFactoryBean) {
        return schedulerFactoryBean.getScheduler();
    }

}
