package net.tsz.afinal.bitmap.core;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 线程安全的LRULinkedHashMap
 * 作者： huangbiao
 * 时间： 2017-03-30
 */
public class LRULinkedHashMap<K, V> extends LinkedHashMap<K, V> {
    private final int MAX_SIZE = 1000;
    private final Lock lock = new ReentrantLock();

    public LRULinkedHashMap() {
        super(1, 1, true);
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry eldest) {
        return size() > MAX_SIZE;
    }

    @Override

    public V get(Object key) {
        try {
            lock.lock();
            return super.get(key);
        } finally {
            lock.unlock();
        }
    }

    @Override

    public V put(K key, V value) {
        try {
            lock.lock();
            return super.put(key, value);
        } finally {
            lock.unlock();
        }
    }
}
