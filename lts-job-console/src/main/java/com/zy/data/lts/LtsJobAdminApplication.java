package com.zy.data.lts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * @author chenqingsong
 * @date 2019/4/1 16:33
 */
@SpringBootApplication
@ComponentScan("com.zy.data.lts.*")
@EnableTransactionManagement
@EnableAsync
public class LtsJobAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(LtsJobAdminApplication.class, args);
    }

    @Bean
    public PlatformTransactionManager txManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
