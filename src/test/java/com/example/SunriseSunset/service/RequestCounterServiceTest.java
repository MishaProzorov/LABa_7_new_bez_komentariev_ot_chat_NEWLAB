package com.example.SunriseSunset.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RequestCounterServiceTest {

    private RequestCounterService counterService;

    @BeforeEach
    public void setUp() {
        counterService = new RequestCounterService();
    }

    @Test
    public void testIncrement() {
        counterService.increment();
        assertEquals(1, counterService.getCount());
    }

    @Test
    public void testGetCount() {
        assertEquals(0, counterService.getCount());
        counterService.increment();
        counterService.increment();
        assertEquals(2, counterService.getCount());
    }

    @Test
    public void testReset() {
        counterService.increment();
        counterService.increment();
        counterService.reset();
        assertEquals(0, counterService.getCount());
    }

    @Test
    public void testResetWhenCountIsZero() {
        counterService.reset();
        assertEquals(0, counterService.getCount());
    }

    @Test
    public void testMultipleIncrementsAndReset() {
        counterService.increment();
        counterService.increment();
        counterService.increment();
        assertEquals(3, counterService.getCount());
        counterService.reset();
        assertEquals(0, counterService.getCount());
        counterService.increment();
        assertEquals(1, counterService.getCount());
    }
}