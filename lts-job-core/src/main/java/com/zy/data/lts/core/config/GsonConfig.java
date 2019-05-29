package com.zy.data.lts.core.config;

import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GsonConfig {

    @Bean
    public GsonEncoder gsonEncoder() {
        return new GsonEncoder();
    }

    @Bean
    public GsonDecoder gsonDecoder() {
        return new GsonDecoder();
    }
}
