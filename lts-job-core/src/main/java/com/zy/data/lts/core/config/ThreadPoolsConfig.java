package com.zy.data.lts.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 初始化线程池
 *
 * @author chenqingsong
 * @date 2019/5/24 14:20
 */
@Configuration
@ConfigurationProperties(prefix = "lts.job.thread.pools")
public class ThreadPoolsConfig implements ApplicationContextAware {
    private static Logger logger = LoggerFactory.getLogger(ThreadPoolsConfig.class);

    /**
     * 工作流提交线程池名称，必须配置
     */
    public static final String SUBMIT_FLOW_TASK_THREAD_POOL = "SubmitFlowTask";

    /**
     * executor 回调Master接口线程池
     */
    public static final String MASTER_CALLBACK_THREAD_POOL = "MasterCallback";

    /**
     * Executor 执行任务线程池
     */
    public static final String EXECUTOR_THREAD_POOL = "ExecutorTaskPool";


    private List<ThreadPoolTaskExecutor> executors;

    @Autowired
    ConfigurableApplicationContext applicationContext;

    @PostConstruct
    void init() {

        if (executors == null) {
            logger.warn("Config [lts.job.thread.pools.executors] is empty!!!");
            return;
        }

        for (ThreadPoolTaskExecutor executor : executors) {
            executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
            executor.initialize();
            applicationContext.getBeanFactory().registerSingleton(executor.getThreadNamePrefix(), executor);
        }
    }

    public List<ThreadPoolTaskExecutor> getExecutors() {
        return executors;
    }

    public void setExecutors(List<ThreadPoolTaskExecutor> executors) {
        this.executors = executors;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }
}
