package com.zy.data.lts.core.tool;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Component
@Order(0)
public class SpringContext implements ApplicationContextAware {

    private static Logger logger = LoggerFactory.getLogger(SpringContext.class);

    private static ConfigurableApplicationContext applicationContext;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext context) throws BeansException {
        applicationContext = (ConfigurableApplicationContext) context;
    }

    public static ConfigurableApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }

    public static <T> Map<String, T> getBeans(Class<T> clazz) {
        return applicationContext.getBeansOfType(clazz);
    }

    public static <T> T getBeanByName(String name, Class<T> clazz) {
        return applicationContext.getBean(name, clazz);
    }

    public static <T> Map<String, T> getBeansByType(Class<T> clazz) {
        return applicationContext.getBeansOfType(clazz);
    }

    public static void registerBean(String beanName, Object object) {
        applicationContext.getBeanFactory().registerSingleton(beanName, object);
        if(object instanceof ApplicationListener) {
            applicationContext.addApplicationListener((ApplicationListener) object);
        }
    }

    public static void publishEvent(Object event) {

        applicationContext.publishEvent(event);
    }

    public static <T> T getOrCreateBean(String name, Class<T> clazz ,Object... params)  {
        return getOrCreateBean(name, clazz, null, params);
    }

    public static <T> T getOrCreateBean(String name, Class<T> clazz , Consumer<Boolean> callback, Object... params)  {
        synchronized (clazz) {
            Boolean isCreate = false;
            T ret ;
            try {

                ret = getBeanByName(name, clazz);
            } catch (NoSuchBeanDefinitionException e) {
                try {
                    if(ArrayUtils.isEmpty(params)) {
                        registerBean(name, clazz.newInstance());
                    } else {
                        Class[] classes = Stream.of(params).map(Object::getClass).toArray(Class[]::new);
                        Constructor<T> constructor = clazz.getConstructor(classes);
                        registerBean(name, constructor.newInstance(params));
                    }

                    isCreate = true;
                    ret = getBeanByName(name, clazz);
                } catch (Exception e1) {
                    logger.error("Fail to register bean!", e1);
                    return null;
                }
            }

            if(callback != null) {
                callback.accept(isCreate);
            }

            return ret;
        }
    }
}
