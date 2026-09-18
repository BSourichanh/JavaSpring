package com.bsourichanh.javaspring.service;

import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class RandomHeartbeatSensor implements HeartbeatSensor {
    @Override
    public int get() {
        Random random = new Random();
        return random.nextInt(0, 100);
    }
}
