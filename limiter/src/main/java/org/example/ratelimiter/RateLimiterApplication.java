package org.example.ratelimiter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class RateLimiterApplication {

    @Value("${grpc.port:6565}")
    private int grpcPort;

    public static void main(String[] args) throws IOException, InterruptedException {
        var ctx = SpringApplication.run(RateLimiterApplication.class, args);
        ctx.getBean(RateLimiterApplication.class).startGrpc(ctx);
    }

    private void startGrpc(ConfigurableApplicationContext ctx) throws IOException, InterruptedException {
        RateLimiterService service = ctx.getBean(RateLimiterService.class);

        Server server = NettyServerBuilder.forPort(grpcPort)
                .addService(service)
                .build()
                .start();

        System.out.println("gRPC RateLimiter started on port " + grpcPort);

        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
        server.awaitTermination();
    }
}
