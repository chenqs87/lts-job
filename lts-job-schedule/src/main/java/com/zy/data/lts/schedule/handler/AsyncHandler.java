package com.zy.data.lts.schedule.handler;

import com.zy.data.lts.core.model.ExecuteRequest;
import com.zy.data.lts.core.model.Executor;
import com.zy.data.lts.core.model.KillTaskRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;

/**
 * TODO:::异步执行
 * @author chenqingsong
 * @date 2019/5/13 10:06
 */
public class AsyncHandler implements IHandler {

    private Logger logger = LoggerFactory.getLogger(AsyncHandler.class);

    private final IHandler handler;

    public AsyncHandler(IHandler handler) {
        this.handler = handler;
    }

    @Async
    @Override
    public void execute(ExecuteRequest request) {
        handler.execute(request);
    }

    @Async
    @Override
    public void kill(KillTaskRequest request) {
        handler.kill(request);
    }

    @Async
    @Override
    public void beat(Executor executor) {
        handler.beat(executor);
    }

    @Override
    public void close() {
        try {
            handler.close();
        } catch (Exception ignore) { }
    }
}
