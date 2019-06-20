package com.zy.data.lts.naming.master;

import com.zy.data.lts.core.api.IMaster;
import com.zy.data.lts.core.config.ThreadPoolsConfig;
import com.zy.data.lts.core.dao.FlowTaskDao;
import com.zy.data.lts.core.entity.FlowTask;
import com.zy.data.lts.core.model.BeatInfoRequest;
import com.zy.data.lts.core.model.JobResultRequest;
import com.zy.data.lts.core.tool.SpringContext;
import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 *
 */
@Component
@ConditionalOnProperty(name = "lts.server.role", havingValue = "executor")
public class AsyncMaster implements ApplicationListener<LtsMasterChangeEvent>, IMaster {
    private Logger logger = LoggerFactory.getLogger(AsyncMaster.class);

    private ConcurrentHashMap<String, IMaster> masters = new ConcurrentHashMap<>();
    private Object lock = new Object();

    private AtomicBoolean isRunning = new AtomicBoolean(true);

    @Autowired
    private FlowTaskDao flowTaskDao;

    private String getHost(int flowTaskId) {
        try {
            FlowTask flowTask = flowTaskDao.findById(flowTaskId);
            return flowTask.getHost();
        } catch (Exception e) {
            return null;
        }
    }

    public void lock() {
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException ignore) { }
        }
    }

    private void doExec(Consumer<IMaster> consumer, JobResultRequest request) {

        do {
            String host = getHost(request.getFlowTaskId());

            if (StringUtils.isNotBlank(host)) {
                IMaster master = masters.get(host);
                if (accept(consumer, host, master)) {
                    return;
                }
            }

            for (IMaster master : masters.values()) {
                if (accept(consumer, host, master)) {
                    return;
                }
            }


            lock();
        } while (isRunning.get());
    }

    private boolean accept(Consumer<IMaster> consumer, String host, IMaster master) {
        if (master != null) {
            try {
                consumer.accept(master);
                return true;
            } catch (Exception e) {
                logger.error("Fail to send request to master!", e);
                SpringContext.publishEvent(new MasterUnConnectedEvent(host));
            }
        }
        return false;
    }

    @PreDestroy
    public void destroy() {
        isRunning.set(false);
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    @Async(ThreadPoolsConfig.MASTER_CALLBACK_THREAD_POOL)
    @Override
    public void success(JobResultRequest request) {
        doExec(master -> master.success(request), request);
    }

    @Async(ThreadPoolsConfig.MASTER_CALLBACK_THREAD_POOL)
    @Override
    public void fail(JobResultRequest request) {
        doExec(master -> master.fail(request), request);
    }

    @Async(ThreadPoolsConfig.MASTER_CALLBACK_THREAD_POOL)
    @Override
    public void start(JobResultRequest request) {
        doExec(master -> master.start(request), request);
    }

    @Async(ThreadPoolsConfig.MASTER_CALLBACK_THREAD_POOL)
    @Override
    public void kill(JobResultRequest request) {
        doExec(master -> master.kill(request), request);
    }

    @Override
    public void beat(BeatInfoRequest request) {
        masters.values().forEach(ma -> ma.beat(request));
    }

    public void addNew(String host) {
        masters.computeIfAbsent(host, f -> createMaster(host));
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    public void remove(String host) {
        masters.remove(host);
    }


    @Override
    public void onApplicationEvent(LtsMasterChangeEvent event) {

        switch (event.getEventType()) {
            case NEW: addNew(event.getHost()); break;
            case DELETE: remove(event.getHost()); break;
        }
    }

    private IMaster createMaster(String host) {
        return Feign.builder()
                .encoder(new GsonEncoder())
                .decoder(new GsonDecoder())
                .target(IMaster.class, "http://" + host);
    }

}
