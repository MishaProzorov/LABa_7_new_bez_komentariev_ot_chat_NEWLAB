package com.example.SunriseSunset.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**Configuration class for Swagger OpenAPI documentation.*/
@Configuration
public class SwaggerConfig {

    /**Configures the OpenAPI documentation for the Sunrise Sunset API.*/
    @Bean
    public OpenAPI customOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sunrise Sunset API")
                        .version("1.0.0")
                        .description("API for managing sunrise and sunset times")
                        .contact(new Contact()
                                .name("API Support")
                                .email("support@sunrisesunset.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")));
    }
}