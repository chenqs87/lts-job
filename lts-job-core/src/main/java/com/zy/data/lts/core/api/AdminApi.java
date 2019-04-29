package com.zy.data.lts.core.api;

import com.zy.data.lts.core.api.config.ExecutorApiConfig;
import com.zy.data.lts.core.model.BeatInfoRequest;
import com.zy.data.lts.core.model.JobResultRequest;
import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * AdminServer API 由 executor 调用
 * @author chenqingsong
 * @date 2019/4/1 13:44
 */

@Component
@ConditionalOnProperty(name= "lts.server.role", havingValue = "executor")
public class AdminApi implements IAdminApi, ApplicationListener<WebServerInitializedEvent> {


    @Autowired
    private ExecutorApiConfig executorApiConfig;

    /**
     * 所有 admin service
     */
    private final Map<String, IAdminApi> adminServers = new HashMap<>();

    /**
     * 存活的admin service
     */
    private final Map<String, IAdminApi> activeServers = new ConcurrentHashMap<>();

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private int serverPort;

    @PostConstruct
    public void init() {
        List<String> urls = executorApiConfig.getAdminUrls();
        urls.forEach(url -> {
            IAdminApi adminApi = Feign.builder()
                    .encoder(new GsonEncoder())
                    .decoder(new GsonDecoder())
                    .target(IAdminApi.class, url);
            adminServers.putIfAbsent(url, adminApi);
        });

        executorService.scheduleWithFixedDelay(this::beat, 0, 5, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void destroy() {
        try {
            executorService.shutdownNow();
        } catch (Exception ignore) {}

    }

    public IAdminApi getIAdminApi() {
        if(activeServers.values().iterator().hasNext()) {
            return activeServers.values().iterator().next();
        }

        return null;
    }

    @Override
    public void success(JobResultRequest request) {
        getIAdminApi().success(request);
    }

    @Override
    public void fail(JobResultRequest request) {
        getIAdminApi().fail(request);
    }

    @Override
    public void start(JobResultRequest request) {
        getIAdminApi().start(request);
    }

    @Override
    public void beat(BeatInfoRequest request) {
        adminServers.forEach((k,v) -> {
            try {
                 v.beat(request);
                activeServers.putIfAbsent(k, v);
            } catch (Exception e) {
                activeServers.remove(k);
            }
        });

    }

    private void beat() {
        BeatInfoRequest request = new BeatInfoRequest();
        request.setHandler(executorApiConfig.getHandler());
        request.setPort(serverPort);
        beat(request);
    }

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        serverPort = event.getWebServer().getPort();
    }
}
