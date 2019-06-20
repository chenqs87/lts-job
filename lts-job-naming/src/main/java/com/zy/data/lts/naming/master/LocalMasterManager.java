package com.zy.data.lts.naming.master;

import com.zy.data.lts.core.model.BeatInfoRequest;
import com.zy.data.lts.core.tool.SpringContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Scheduled;

public class LocalMasterManager implements ApplicationListener<WebServerInitializedEvent> {
    private Logger logger = LoggerFactory.getLogger(LocalMasterManager.class);

    @Value("${lts.server.executor.adminUrl}")
    private String adminUrl;

    @Value("${lts.server.executor.handler.name}")
    private String handler;

    @Value("${lts.server.executor.handler.host}")
    private String host;

    @Autowired
    AsyncMaster asyncMaster;

    private int serverPort;

    @Scheduled(fixedDelay = 5000)
    public void beat() {
        if (serverPort == 0) {
            return;
        }

        BeatInfoRequest request = new BeatInfoRequest();
        request.setHandler(handler);
        request.setPort(serverPort);

        try {
            asyncMaster.beat(request);
            send(MasterEventType.NEW);
        } catch (Exception e) {
            logger.error("Fail to connect to Master.Msg: {}", e.getMessage());
            send(MasterEventType.DELETE);
        }
    }

    private void send(MasterEventType type) {
        SpringContext.publishEvent(
                new LtsMasterChangeEvent(adminUrl, type));
    }

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        serverPort = event.getWebServer().getPort();
    }
}
