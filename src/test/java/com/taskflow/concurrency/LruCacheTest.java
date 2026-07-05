package com.taskflow.concurrency;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LruCacheTest {
    @Test
    void evictsLeastRecentlyUsedEntry() {
        LruCache<String, Integer> cache = new LruCache<>(2);
        cache.put("a", 1);
        cache.put("b", 2);
        assertEquals(1, cache.get("a").orElseThrow());

        cache.put("c", 3);

        assertTrue(cache.get("b").isEmpty());
        assertEquals(1, cache.get("a").orElseThrow());
        assertEquals(3, cache.get("c").orElseThrow());
    }
}
