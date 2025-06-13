package com.example.SunriseSunset.dto;

/**Represents the response model for sunrise and sunset data from the external API.*/
public class SunriseSunsetModel {

    private Results results;

    private String status;

    public Results getResults() {
        return results;
    }

    public void setResults(Results results) { this.results = results;}

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}