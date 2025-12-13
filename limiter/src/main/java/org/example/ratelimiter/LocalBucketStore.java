package org.example.ratelimiter;


import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class LocalBucketStore {

    private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    private final long capacity = 10;
    private final double refillPerSec = 5.0;

    public RateLimitDecision check(String key, long nowMillis) {
        TokenBucket bucket = buckets.computeIfAbsent(
                key,
                k -> new TokenBucket(capacity, refillPerSec)
        );

        boolean allowed = bucket.tryConsume(nowMillis);
        long retryAfter = bucket.getRetryAfterMillis(nowMillis);
        return new RateLimitDecision(allowed, retryAfter);
    }

    public static class RateLimitDecision {
        private final boolean allowed;
        private final long retryAfterMillis;

        public RateLimitDecision(boolean allowed, long retryAfterMillis) {
            this.allowed = allowed;
            this.retryAfterMillis = retryAfterMillis;
        }

        public boolean allowed() {
            return allowed;
        }

        public long retryAfterMillis() {
            return retryAfterMillis;
        }
    }
}