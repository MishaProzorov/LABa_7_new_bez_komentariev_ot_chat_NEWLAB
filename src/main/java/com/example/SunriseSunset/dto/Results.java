package com.example.SunriseSunset.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**Represents the sunrise and sunset times returned by the external API.*/
public class Results {

    private String sunrise;

    private String sunset;

    @JsonProperty("sunrise")
    public String getSunrise() {
        return sunrise;
    }

    /**Gets the sunset time.*/
    @JsonProperty("sunset")
    public String getSunset() {
        return sunset;
    }
    public void setSunrise(String sunrise) { this.sunrise = sunrise; }
    public void setSunset(String sunset) { this.sunset = sunset; }
}