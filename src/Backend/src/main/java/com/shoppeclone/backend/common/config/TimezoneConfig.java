package com.shoppeclone.backend.common.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.TimeZone;

/**
 * Force JVM timezone to Asia/Ho_Chi_Minh (GMT+7) at startup.
 * This ensures ALL LocalDateTime.now() calls across the system
 * return Vietnam time consistently.
 */
@Component
@Slf4j
public class TimezoneConfig {

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        log.info("JVM timezone forced to: {} ({})",
                TimeZone.getDefault().getID(),
                TimeZone.getDefault().getDisplayName());
    }
}

