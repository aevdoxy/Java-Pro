package com.example.limits.config;

import com.example.limits.service.LimitService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class SchedulerConfig {

    private final LimitService limitService;

    public SchedulerConfig(LimitService limitService) { this.limitService = limitService; }

    // Runs every day at 00:00 server time
    @Scheduled(cron = "0 0 0 * * *")
    public void resetLimits() {
        limitService.resetAllToDefault();
    }
}\n