package com.finance.tracker.service.impl;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class KeepAliveScheduler {

    private final RestTemplate restTemplate = new RestTemplate();

    @Scheduled(cron = "0 */10 * * * *")
    public void keepAlive() {
        try {
            restTemplate.getForObject(
                    "http://localhost:${server.port}/actuator/health",
                    String.class
            );
        } catch (Exception e) {
            // Log the exception if needed, but ignore it to prevent the scheduler from failing
            System.err.println("Keep-alive request failed: " + e.getMessage());
        }
    }
}