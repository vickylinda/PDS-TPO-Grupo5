package org.example.security;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimiter {
    private final int maxEvents;
    private final long windowMillis;
    private final Map<String, Deque<Long>> buckets = new ConcurrentHashMap<>();

    public RateLimiter(int maxEvents, long windowMillis) {
        this.maxEvents = maxEvents;
        this.windowMillis = windowMillis;
    }

    public synchronized boolean allow(String key) {
        long now = Instant.now().toEpochMilli();
        Deque<Long> q = buckets.computeIfAbsent(key, k -> new ArrayDeque<>());
        while (!q.isEmpty() && now - q.peekFirst() > windowMillis) q.pollFirst();
        if (q.size() >= maxEvents) return false;
        q.addLast(now);
        return true;
    }

    public synchronized long retryAfterSeconds(String key){
        long now = Instant.now().toEpochMilli();
        Deque<Long> q = buckets.get(key);
        if (q == null || q.isEmpty()) return 0;
        long first = q.peekFirst();
        long left = windowMillis - (now - first);
        return left <= 0 ? 0 : (left + 999) / 1000;
    }
}

