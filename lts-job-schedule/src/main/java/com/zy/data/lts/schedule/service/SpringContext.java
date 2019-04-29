package com.zy.data.lts.schedule.service;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@Order(0)
public class SpringContext implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext context) throws BeansException {
        applicationContext = context;
    }

    public <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }

}
