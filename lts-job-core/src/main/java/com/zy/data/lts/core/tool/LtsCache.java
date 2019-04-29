package com.zy.data.lts.core.tool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.*;

/**
 * @author chenqingsong
 * @date 2019/4/9 15:57
 */
public class LtsCache<K, V> extends ConcurrentHashMap<K, V> {

    private static final long OUT_TIME = 2 * 60 * 1000;
    private final Map<Object, Long> time = new ConcurrentHashMap<>();
    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    private volatile List<V> values = null;
    private int count;
    public V put(K key, V value) {
        lock.lock();
        try {
            time.put(key, System.currentTimeMillis());

            V ret = super.put(key, value);
            List<V> list = new ArrayList<>(super.size());
            values().forEach(v -> list.add(ret));

            values = list;

            return ret;
        } finally {
            lock.unlock();
        }

    }

    public V rollSelect() {
        lock.lock();

        try {

            if(values == null) {
                condition.await();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return null;

    }
}
