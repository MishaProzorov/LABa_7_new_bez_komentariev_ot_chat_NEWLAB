package com.example.SunriseSunset.cache;

import java.util.HashMap;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**Configuration class for defining cache-related beans.*/
@Configuration
public class Cache {

    /**Creates a HashMap to be used as an entity cache.*/
    @Bean
    public Map<String, Object> entityCache() {
        return new HashMap<>();
    }
}