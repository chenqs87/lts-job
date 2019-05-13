package com.zy.data.lts.core.api;

import com.zy.data.lts.core.api.config.ExecutorApiConfig;
import com.zy.data.lts.core.model.BeatInfoRequest;
import com.zy.data.lts.core.model.JobResultRequest;
import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AdminServer API 由 executor 调用
 *
 * @author chenqingsong
 * @date 2019/4/1 13:44
 */

@Component
@ConditionalOnProperty(name = "lts.server.role", havingValue = "executor")
public class AdminApi implements IAdminApi, ApplicationListener<WebServerInitializedEvent> {
    private final static Logger logger = LoggerFactory.getLogger(AdminApi.class);
    private final AtomicBoolean isRunning = new AtomicBoolean(true);
    /**
     * 所有 admin service
     */
    private final Map<String, IAdminApi> adminServers = new HashMap<>();
    /**
     * 存活的admin service
     */
    private final Map<String, IAdminApi> activeServers = new ConcurrentHashMap<>();
    private final ScheduledExecutorService beatService = Executors.newSingleThreadScheduledExecutor();
    private final ExecutorService taskStatusService = Executors.newSingleThreadExecutor();
    @Autowired
    private ExecutorApiConfig executorApiConfig;
    @Autowired
    private DefaultAdminApi defaultAdminApi;
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

        beatService.scheduleWithFixedDelay(this::beat, 0, 5, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void destroy() {
        try {
            isRunning.set(true);
            this.notifyAll();
            taskStatusService.shutdownNow();
            beatService.shutdownNow();
        } catch (Exception ignore) {
        }

    }

    public IAdminApi getMasterAdminApi() {

        while (isRunning.get()) {
            // TODO master 高可用时，需要调整，当前适合单Master
            if (activeServers.values().iterator().hasNext()) {
                return activeServers.values().iterator().next();
            }

            synchronized (this) {
                try {
                    logger.info("Active Masters is 0!");
                    this.wait();
                } catch (InterruptedException ignore) {
                }
            }
        }

        return defaultAdminApi;

    }

    @Override
    public void success(JobResultRequest request) {
        try {
            getMasterAdminApi().success(request);
        } catch (Exception e) {
            e.printStackTrace();
            execute(request, () -> success(request));
        }

    }

    @Override
    public void fail(JobResultRequest request) {

        try {
            getMasterAdminApi().fail(request);
        } catch (Exception e) {
            execute(request, () -> fail(request));
        }
    }

    public void execute(JobResultRequest request, Runnable runnable) {
        if (request.getAndIncrement() < 10) {
            taskStatusService.execute(runnable);
        }

    }

    @Override
    public void start(JobResultRequest request) {

        try {
            getMasterAdminApi().start(request);
        } catch (Exception e) {
            execute(request, () -> start(request));
        }

    }

    @Override
    public void kill(JobResultRequest request) {
        try {
            getMasterAdminApi().kill(request);
        } catch (Exception e) {
            execute(request, () -> kill(request));
        }
    }

    @Override
    public void beat(BeatInfoRequest request) {
        adminServers.forEach((k, v) -> {
            try {
                v.beat(request);
                activeServers.putIfAbsent(k, v);

                synchronized (this) {
                    this.notifyAll();
                }

            } catch (Exception e) {
                logger.warn("Fail to connect to master [{}]", k);
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
