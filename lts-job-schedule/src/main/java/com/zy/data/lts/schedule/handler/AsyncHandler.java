package com.zy.data.lts.schedule.handler;

import com.zy.data.lts.core.model.ExecuteRequest;
import com.zy.data.lts.core.model.Executor;
import com.zy.data.lts.core.model.KillTaskRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.*;

/**
 * TODO:::异步执行
 * @author chenqingsong
 * @date 2019/5/13 10:06
 */
public class AsyncHandler implements IHandler {

    private Logger logger = LoggerFactory.getLogger(AsyncHandler.class);

    private final ExecutorService executorService ;
    private final IHandler handler;

    public AsyncHandler(IHandler handler) {
        this.handler = handler;
        this.executorService = Executors.newFixedThreadPool(2);
    }

    @Override
    public void execute(ExecuteRequest request) {
        executorService.execute(() -> handler.execute(request));
    }

    @Override
    public void kill(KillTaskRequest request) {
        executorService.execute(() -> handler.kill(request));
    }

    @Override
    public void beat(Executor executor) {
        executorService.execute(() -> handler.beat(executor));
    }

    @Override
    public void close() {
        try {
            executorService.shutdown();
            handler.close();
        } catch (Exception ignore) { }
    }
}
