package com.zy.data.lts.executor.type;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author chenqingsong
 * @date 2019/5/23 10:55
 */
@Documented
@Inherited
@Retention(RUNTIME)
@Target(TYPE)
@Component
public @interface JobType {

    @AliasFor(annotation = Component.class)
    String value();
}
