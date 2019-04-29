package com.zy.data.lts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author chenqingsong
 * @date 2019/4/1 16:33
 */
@SpringBootApplication
@ComponentScan("com.zy.data.lts.*")
public class LtsJobAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(LtsJobAdminApplication.class, args);
    }
}
