package com.zy.data.lts.naming;

import com.sun.prism.shader.Solid_TextureYV12_AlphaTest_Loader;
import com.zy.data.lts.core.api.IExecutorApi;
import com.zy.data.lts.core.model.BeatInfoRequest;
import com.zy.data.lts.core.model.Executor;
import com.zy.data.lts.core.tool.SpringContext;
import com.zy.data.lts.naming.handler.AsyncHandler;
import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LocalHandlerManager {

    @Autowired
    private GsonEncoder gsonEncoder;

    @Autowired
    private GsonDecoder gsonDecoder;

    /**
     * 服务发现本地模式需要心跳，zk模式不需要
     * @param beat
     */
    public void beat(BeatInfoRequest beat) {
        if (beat.getPort() > 0) {
            updateExecutors(beat);
        }
    }

    //<host:port, Executor>
    private final Map<String, Executor> executorMap = new ConcurrentHashMap<>();

    private void updateExecutors(BeatInfoRequest beat) {

        String hostAndPort = beat.getHost() + ":" + beat.getPort();
        executorMap.computeIfPresent(hostAndPort, (k, v) -> {
            v.setLastUpdateTime(System.currentTimeMillis());
            if(!v.getHandler().equals(beat.getHandler())) {
                SpringContext.publishEvent(new LtsHandlerChangeEvent(v.getHandler(), HandlerEventType.DELETE, null));
                v.setHandler(beat.getHandler());
            }

            return v;
        });

        executorMap.computeIfAbsent(hostAndPort, f -> createExecutor(beat));
    }

    private Executor createExecutor(BeatInfoRequest beat) {
        Executor executor = new Executor();
        String host = beat.getHost() + ":" + beat.getPort();
        IExecutorApi api = Feign.builder()
                .encoder(gsonEncoder)
                .decoder(gsonDecoder)
                .target(IExecutorApi.class, "http://" + host);
        executor.setApi(api);
        executor.setHost(host);
        executor.setHandler(beat.getHandler());

        SpringContext.getOrCreateBean(beat.getHandler() + AsyncHandler.class.getName(), AsyncHandler.class, beat.getHandler());
        SpringContext.publishEvent(new LtsHandlerChangeEvent(beat.getHandler(), HandlerEventType.NEW ,executor));

        return executor;
    }

    @EventListener
    public void handle(ExecutorUnConnectedEvent event) {
        executorMap.remove(event.getExecutor().getHost());
        SpringContext.publishEvent(new LtsHandlerChangeEvent(event.getExecutor().getHandler(), HandlerEventType.DELETE ,event.getExecutor()));
    }
}
