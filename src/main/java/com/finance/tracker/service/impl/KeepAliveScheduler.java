package com.finance.tracker.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class KeepAliveScheduler {

    private final RestTemplate restTemplate = new RestTemplate();


    @Value("${server.port}")
    private String port;

    @Scheduled(cron = "0 */10 * * * *")
    public void keepAlive() {
        try {
            String url = "http://localhost:" + port + "/actuator/health";
            restTemplate.getForObject(url, String.class);
            System.out.println("Keep alive ping sent");
        } catch (Exception e) {
            System.out.println("Keep-alive request failed: " + e.getMessage());
        }
    }
}