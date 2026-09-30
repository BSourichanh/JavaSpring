package com.bsourichanh.javaspring.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
public class RandomHeartbeatSensor implements HeartbeatSensor {
    @Override
    public int get() {
        return ThreadLocalRandom.current().nextInt(0, 100);
    }
}
