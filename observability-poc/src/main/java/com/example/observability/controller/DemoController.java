package com.example.observability.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/api/demo")
public class DemoController {

    @GetMapping("/slow")
    public Map<String, Object> slow() throws InterruptedException {
        long delay = ThreadLocalRandom.current().nextLong(200, 1500);
        Thread.sleep(delay);
        return Map.of("status", "ok", "delayMs", delay);
    }

    @GetMapping("/error")
    public Map<String, Object> error() {
        throw new IllegalStateException("Simulated failure for the Grafana error-rate panel");
    }
}
