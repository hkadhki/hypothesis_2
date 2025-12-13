package org.example.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController()
@SpringBootApplication
public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }

    @GetMapping("/dht/test")
    public Map<String, Object> demo(@RequestHeader(value = "X-User-Id", required = false) String userId) {
        return Map.of(
                "message", "Backend response OK",
                "userId", userId,
                "timestamp", System.currentTimeMillis()
        );
    }


    @GetMapping("/redis/test")
    public Map<String, Object> demoRedis(@RequestHeader(value = "X-User-Id", required = false) String userId) {
        return Map.of(
                "message", "Backend response OK",
                "userId", userId,
                "timestamp", System.currentTimeMillis()
        );
    }

}
