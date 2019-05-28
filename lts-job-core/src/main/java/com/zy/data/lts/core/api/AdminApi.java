package com.zy.data.lts.core.api;

import com.zy.data.lts.core.api.config.ExecutorApiConfig;
import com.zy.data.lts.core.config.ThreadPoolsConfig;
import com.zy.data.lts.core.model.BeatInfoRequest;
import com.zy.data.lts.core.model.JobResultRequest;
import com.zy.data.lts.core.tool.SpringContext;
import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

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

    private IAdminApi adminServer;
    private volatile boolean hasAdminServer = false;

    @Autowired
    private ExecutorApiConfig executorApiConfig;

    @Autowired
    private DefaultAdminApi defaultAdminApi;

    private int serverPort;

    @PostConstruct
    public void init() {
        String url = executorApiConfig.getAdminUrl();
        adminServer = Feign.builder()
                .encoder(new GsonEncoder())
                .decoder(new GsonDecoder())
                .target(IAdminApi.class, url);
    }

    @Scheduled(fixedDelay = 5000)
    public void beat() {
        if (serverPort == 0) {
            return;
        }

        BeatInfoRequest request = new BeatInfoRequest();
        request.setHandler(executorApiConfig.getHandler());
        request.setPort(serverPort);
        beat(request);
    }

    @PreDestroy
    public void destroy() {
        try {
            isRunning.set(false);
            synchronized (this) {
                this.notifyAll();
            }
        } catch (Exception ignore) {
        }
    }

    public IAdminApi getMasterAdminApi() {

        while (isRunning.get()) {
            if (hasAdminServer) {
                return adminServer;
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

    @Async(ThreadPoolsConfig.MASTER_CALLBACK_THREAD_POOL)
    @Override
    public void success(JobResultRequest request) {
        try {
            getMasterAdminApi().success(request);
        } catch (Exception e) {
            logger.warn("Fail to send success to master！Params:[{}]", request);
            execute(request, this::success);
        }

    }

    @Async(ThreadPoolsConfig.MASTER_CALLBACK_THREAD_POOL)
    @Override
    public void fail(JobResultRequest request) {
        try {
            getMasterAdminApi().fail(request);
        } catch (Exception e) {
            logger.warn("Fail to send fail to master！Params:[{}]", request, e);
            execute(request, this::fail);
        }
    }

    public void execute(JobResultRequest request, Consumer<JobResultRequest> consumer) {
        AdminApi adminApi = SpringContext.getBean(AdminApi.class);
        adminApi.doExecute(request, consumer);
    }

    @Async(ThreadPoolsConfig.MASTER_CALLBACK_THREAD_POOL)
    public void doExecute(JobResultRequest request, Consumer<JobResultRequest> consumer) {
        if (request.getAndIncrement() < 10) {
            consumer.accept(request);
        }
    }

    @Async(ThreadPoolsConfig.MASTER_CALLBACK_THREAD_POOL)
    @Override
    public void start(JobResultRequest request) {

        try {
            getMasterAdminApi().start(request);
        } catch (Exception e) {
            logger.warn("Fail to send start to master！Params:[{}]", request, e);
            execute(request, this::start);
        }

    }

    @Async(ThreadPoolsConfig.MASTER_CALLBACK_THREAD_POOL)
    @Override
    public void kill(JobResultRequest request) {
        try {
            getMasterAdminApi().kill(request);
        } catch (Exception e) {
            logger.warn("Fail to send kill to master！Params:[{}]", request, e);
            execute(request, this::kill);
        }
    }

    @Override
    public void beat(BeatInfoRequest request) {

        try {
            adminServer.beat(request);
            if (!hasAdminServer) {
                synchronized (this) {
                    this.notifyAll();
                }
                hasAdminServer = true;
            }
        } catch (Exception e) {
            logger.warn("Fail to connect to master [{}]", adminServer);
            hasAdminServer = false;
        }
    }

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        serverPort = event.getWebServer().getPort();
    }

}
