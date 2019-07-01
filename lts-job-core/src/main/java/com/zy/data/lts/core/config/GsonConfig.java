package com.zy.data.lts.core.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GsonConfig {

    @Bean
    public GsonEncoder gsonEncoder(Gson gson) {
        return new GsonEncoder(gson);
    }

    @Bean
    public GsonDecoder gsonDecoder(Gson gson) {
        return new GsonDecoder(gson);
    }

    @Bean
    public Gson getGson() {
        return new GsonBuilder()
                .setDateFormat("yyyy-MM-dd hh:mm:ss")
                .create();
    }
}
