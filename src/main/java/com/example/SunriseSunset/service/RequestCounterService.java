package com.example.SunriseSunset.service;

import org.springframework.stereotype.Service;

@Service
public class RequestCounterService {
    private long count = 0;

    public synchronized void increment() {
        count++;
    }

    public synchronized long getCount() {
        return count;
    }

    public synchronized void reset() {
        count = 0;
    }
}