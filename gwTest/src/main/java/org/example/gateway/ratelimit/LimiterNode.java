package org.example.gateway.ratelimit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.example.ratelimiter.RateLimiterGrpc;

public class LimiterNode {

    private final String id;
    private final String host;
    private final int port;

    private final ManagedChannel channel;
    private final RateLimiterGrpc.RateLimiterBlockingStub stub;

    public LimiterNode(String id, String host, int port) {
        this.id = id;
        this.host = host;
        this.port = port;

        this.channel = ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext()
                .build();

        this.stub = RateLimiterGrpc.newBlockingStub(channel);
    }

    public String id() {
        return id;
    }

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    public RateLimiterGrpc.RateLimiterBlockingStub stub() {
        return stub;
    }

    public void shutdown() {
        channel.shutdownNow();
    }
}
