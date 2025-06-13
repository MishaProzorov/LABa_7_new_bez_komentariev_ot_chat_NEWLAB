package com.example.SunriseSunset.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**Configuration class for defining REST client-related beans.*/
@Configuration
public class SunriseSunsetConfiguration {

    /**Creates a RestTemplate bean for making HTTP requests.*/
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}