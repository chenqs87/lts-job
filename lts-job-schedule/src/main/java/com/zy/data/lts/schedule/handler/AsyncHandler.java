package com.zy.data.lts.schedule.handler;

import com.zy.data.lts.core.model.ExecuteRequest;
import com.zy.data.lts.core.model.Executor;
import com.zy.data.lts.core.model.KillTaskRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

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
        this.executorService = new ThreadPoolExecutor(2,
                2, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(), new HandlerThreadFactory(handler.name()));
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
         handler.beat(executor);
    }

    @Override
    public void close() {
        try {
            executorService.shutdown();
            handler.close();
        } catch (Exception ignore) { }
    }

    static class HandlerThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        HandlerThreadFactory(String handlerName) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = "Handler[" +handlerName + "]-" +
                    poolNumber.getAndIncrement() +
                    "-thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}
