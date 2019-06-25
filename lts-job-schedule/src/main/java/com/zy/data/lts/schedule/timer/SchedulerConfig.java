package com.zy.data.lts.schedule.timer;

import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * @author chenqingsong
 * @date 2019/3/27 20:38
 */

@Configuration
@ConfigurationProperties(prefix = "lts.quartz")
public class SchedulerConfig {

    private Properties config;

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
        schedulerFactory.setQuartzProperties(config);

        return schedulerFactory;
    }

    @Bean(name = "scheduler")
    public Scheduler scheduler(@Qualifier("ltsSchedulerFactoryBean") SchedulerFactoryBean schedulerFactoryBean) {
        return schedulerFactoryBean.getScheduler();
    }

    public Properties getConfig() {
        return config;
    }

    public void setConfig(Properties config) {
        this.config = config;
    }
}
