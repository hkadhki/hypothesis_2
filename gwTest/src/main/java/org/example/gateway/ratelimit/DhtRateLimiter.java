package org.example.gateway.ratelimit;

import org.example.ratelimiter.RateLimitRequest;
import org.example.ratelimiter.RateLimitResponse;
import org.example.ratelimiter.RateLimiterGrpc;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.*;

@Component("dhtRateLimiter")
@Primary
public class DhtRateLimiter implements RateLimiter<DhtRateLimiter.Config> {

    public static class Config {
    }

    private final List<LimiterNode> nodes;
    private final ConsistentHashRing ring;

    public DhtRateLimiter() {
        String raw = System.getenv("RATELIMITER_NODES");
        if (raw == null || raw.isBlank()) {
            throw new IllegalStateException("RATELIMITER_NODES is not set");
        }


        List<LimiterNode> list = new ArrayList<>();
        String[] parts = raw.split("[;\\n]+");
        for (String p : parts) {
            p = p.trim();
            if (p.isEmpty()) continue;

            String[] hostPort = p.split(":");
            if (hostPort.length != 2) {
                throw new IllegalArgumentException("Bad node definition: " + p);
            }
            String host = hostPort[0].trim();
            int port = Integer.parseInt(hostPort[1].trim());
            // id = host, можно усложнить при желании
            list.add(new LimiterNode(host, host, port));
        }

        if (list.isEmpty()) {
            throw new IllegalStateException("No limiter nodes parsed from RATELIMITER_NODES");
        }

        this.nodes = List.copyOf(list);
        this.ring = new ConsistentHashRing(this.nodes);

        System.out.println("DhtRateLimiter initialized with nodes: " + nodes.size());
        nodes.forEach(n -> System.out.println("  " + n.id() + " -> " + n.host() + ":" + n.port()));
    }

    @Override
    public Mono<Response> isAllowed(String routeId, String key) {
        return Mono.fromSupplier(() -> {
            LimiterNode node = ring.getNodeForKey(key);
            RateLimiterGrpc.RateLimiterBlockingStub stub = node.stub();

            RateLimitRequest request = RateLimitRequest.newBuilder()
                    .setKey(key)
                    .setRuleId(routeId)
                    .setTimestamp(System.currentTimeMillis())
                    .build();

            RateLimitResponse resp;
            try {
                resp = stub.checkLimit(request);
            } catch (Exception e) {
                Map<String, String> headers = Map.of(
                        "X-RateLimit-Error", e.getClass().getSimpleName() + ": " + e.getMessage()
                );
                return new Response(true, headers); // fail-open
            }

            boolean allowed = resp.getAllowed();
            Map<String, String> headers = new HashMap<>();
            headers.put("X-RateLimit-Node", node.id());
            headers.put("X-RateLimit-Allowed", Boolean.toString(allowed));
            headers.put("X-RateLimit-Reason", resp.getReason());
            headers.put("X-RateLimit-Retry-After-Millis", Long.toString(resp.getRetryAfterMillis()));

            return new Response(allowed, headers);
        });
    }

    @Override
    public Map<String, Config> getConfig() {
        return Collections.emptyMap();
    }

    @Override
    public Class<Config> getConfigClass() {
        return Config.class;
    }

    @Override
    public Config newConfig() {
        return new Config();
    }
}
