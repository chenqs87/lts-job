package com.zy.data.lts.schedule.handler;

import com.zy.data.lts.core.api.IExecutorApi;
import com.zy.data.lts.core.model.ExecuteRequest;
import com.zy.data.lts.core.model.Executor;
import com.zy.data.lts.core.model.KillTaskRequest;
import com.zy.data.lts.core.model.UpdateTaskHostEvent;
import feign.RetryableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * @author chenqingsong
 * @date 2019/5/13 10:06
 */
public class HandlerApi implements IExecutorApi, IHandler {

    private Logger logger = LoggerFactory.getLogger(HandlerApi.class);

    private final IHandler handler;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private ApplicationContext applicationContext;


    public HandlerApi(IHandler handler, ApplicationContext applicationContext) {
        this.handler = handler;
        this.applicationContext = applicationContext;
    }

    @Override
    public void execute(ExecuteRequest request) {
        if (executorService.isShutdown()) {
            return;
        }

        executorService.execute(() ->
                asyncExec(executor -> {
                    try {
                        applicationContext.publishEvent(
                                new UpdateTaskHostEvent(request.getFlowTaskId(), request.getTaskId(), executor.getHost()));

                        executor.getApi().execute(request);
                    } catch (Exception e) {
                        //失败重发
                        logger.warn("Fail to asyncExec Job", e);
                        if (e instanceof RetryableException) {
                            remove(executor.getHost());
                        }

                        execute(request);
                    }
                })
        );
    }

    @Override
    public void kill(KillTaskRequest request) {
        executorService.execute(() -> {
            try {
                Executor executor = getExecutor(request.getHost());
                if (executor != null) {
                    executor.getApi().kill(request);
                }
            } catch (Exception e) {
                logger.warn("Fail to kill the job [{}] ", request, e);
            }
        });
    }

    @Override
    public void asyncExec(Consumer<Executor> consumer) {
        handler.asyncExec(consumer);
    }

    @Override
    public void beat(Executor executor) {
        handler.beat(executor);
    }

    @Override
    public void remove(String host) {
        handler.remove(host);
    }

    @Override
    public Executor getExecutor(String host) {
        return handler.getExecutor(host);
    }

    @Override
    public void close() {
        try {
            executorService.shutdownNow();
            handler.close();
        } catch (Exception ignore) {
        }
    }
}
