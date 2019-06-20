package com.zy.data.lts.naming.handler;

import com.zy.data.lts.core.model.BeatInfoRequest;
import com.zy.data.lts.core.tool.SpringContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LocalHandlerManager {

    /**
     * 服务发现本地模式需要心跳，zk模式不需要
     * @param beat
     */
    public void beat(BeatInfoRequest beat) {
        if (beat.getPort() > 0) {
            updateExecutors(beat);
        }
    }

    //<host:port, handlerName>
    private final Map<String, String> executorMap = new ConcurrentHashMap<>();

    private void updateExecutors(BeatInfoRequest beat) {

        String hostAndPort = beat.getHost() + ":" + beat.getPort();
        executorMap.computeIfPresent(hostAndPort, (host, handler) -> {
            if(!handler.equals(beat.getHandler())) {
                SpringContext.publishEvent(new LtsHandlerChangeEvent(handler, HandlerEventType.DELETE, hostAndPort));
            }
            return handler;
        });

        executorMap.computeIfAbsent(hostAndPort, f -> {
            SpringContext.getOrCreateBean(beat.getHandler() + AsyncHandler.class.getName(), AsyncHandler.class, beat.getHandler());
            SpringContext.publishEvent(new LtsHandlerChangeEvent(beat.getHandler(), HandlerEventType.NEW , hostAndPort));
            return beat.getHandler();
        });
    }

    @EventListener
    public void handle(ExecutorUnConnectedEvent event) {
        executorMap.remove(event.getExecutor().getHost());
        SpringContext.publishEvent(
                new LtsHandlerChangeEvent(event.getExecutor().getHandler(), HandlerEventType.DELETE ,event.getExecutor().getHost()));
    }
}
