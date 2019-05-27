package com.zy.data.lts.core.tool;

import com.sun.org.apache.regexp.internal.RE;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@Order(0)
public class SpringContext implements ApplicationContextAware {

    private static ConfigurableApplicationContext applicationContext;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext context) throws BeansException {
        applicationContext = (ConfigurableApplicationContext) context;
    }

    public ConfigurableApplicationContext getApplicationContext() {
        return  applicationContext;
    }

    public static <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }

    public static <T> T getBeanByName(String name, Class<T> clazz) {
        return applicationContext.getBean(name, clazz);
    }

    public void registerBean(String beanName, Object object) {
        applicationContext.getBeanFactory().registerSingleton(beanName, object);
    }

}
