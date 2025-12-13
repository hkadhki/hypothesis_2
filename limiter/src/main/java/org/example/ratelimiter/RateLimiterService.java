package org.example.ratelimiter;

import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

@Service
public class RateLimiterService extends RateLimiterGrpc.RateLimiterImplBase {

    private final LocalBucketStore store;

    public RateLimiterService(LocalBucketStore store) {
        this.store = store;
    }

    @Override
    public void checkLimit(RateLimitRequest request,
                           StreamObserver<RateLimitResponse> responseObserver) {

        long now = request.getTimestamp() != 0 ? request.getTimestamp() : System.currentTimeMillis();

        LocalBucketStore.RateLimitDecision decision = store.check(request.getKey(), now);

        RateLimitResponse response = RateLimitResponse.newBuilder()
                .setAllowed(decision.allowed())
                .setReason(decision.allowed() ? "OK" : "LIMIT_EXCEEDED")
                .setRetryAfterMillis(decision.retryAfterMillis())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
