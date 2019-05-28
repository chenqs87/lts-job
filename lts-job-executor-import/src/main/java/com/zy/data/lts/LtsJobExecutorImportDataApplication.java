package com.zy.data.lts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author chenqingsong
 * @date 2019/4/1 16:33
 */
@SpringBootApplication
@ComponentScan("com.zy.data.lts.*")
@EnableAsync(proxyTargetClass = true)
@EnableScheduling
public class LtsJobExecutorImportDataApplication {

    public static void main(String[] args) {
        SpringApplication.run(LtsJobExecutorImportDataApplication.class, args);
    }
}
