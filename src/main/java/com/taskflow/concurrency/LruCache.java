package com.taskflow.concurrency;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Thread-safe LRU cache backed by access-ordered {@link LinkedHashMap}.
 *
 * @param <K> key type
 * @param <V> value type
 */
public final class LruCache<K, V> {
    private final Map<K, V> delegate;

    public LruCache(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        this.delegate = new LinkedHashMap<>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > capacity;
            }
        };
    }

    public synchronized void put(K key, V value) {
        delegate.put(key, value);
    }

    public synchronized Optional<V> get(K key) {
        return Optional.ofNullable(delegate.get(key));
    }

    public synchronized void invalidate(K key) {
        delegate.remove(key);
    }

    public synchronized int size() {
        return delegate.size();
    }
}
