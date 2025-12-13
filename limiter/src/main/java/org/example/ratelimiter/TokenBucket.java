package org.example.ratelimiter;

public class TokenBucket {
    private final long capacity;
    private final double refillPerSec;

    private double tokens;
    private long lastRefillMillis;

    public TokenBucket(long capacity, double refillPerSec) {
        this.capacity = capacity;
        this.refillPerSec = refillPerSec;
        this.tokens = capacity;
        this.lastRefillMillis = System.currentTimeMillis();
    }

    synchronized public boolean tryConsume(long nowMillis) {
        refill(nowMillis);
        if (tokens >= 1.0) {
            tokens -= 1.0;
            return true;
        }
        return false;
    }

    synchronized public long getRetryAfterMillis(long nowMillis) {
        refill(nowMillis);
        if (tokens >= 1.0) {
            return 0;
        }
        double need = 1.0 - tokens;
        long ms = (long) (need / refillPerSec * 1000);
        return Math.max(ms, 0);
    }

    private void refill(long nowMillis) {
        long elapsedMs = nowMillis - lastRefillMillis;
        if (elapsedMs <= 0) return;
        double added = (elapsedMs / 1000.0) * refillPerSec;
        tokens = Math.min(capacity, tokens + added);
        lastRefillMillis = nowMillis;
    }
}