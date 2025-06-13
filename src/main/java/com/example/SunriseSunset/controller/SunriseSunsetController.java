package com.example.SunriseSunset.controller;

import com.example.SunriseSunset.dto.SunriseSunsetDto;
import com.example.SunriseSunset.service.SunriseSunsetService;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**REST controller for managing sunrise and sunset data.*/
@RestController
@RequestMapping("/sun/times")
public class SunriseSunsetController {

    private final SunriseSunsetService sunService;

    public SunriseSunsetController(SunriseSunsetService sunService) {
        this.sunService = sunService;
    }

    @PostMapping
    public ResponseEntity<SunriseSunsetDto> createSunriseSunset(@RequestBody SunriseSunsetDto dto) {
        return ResponseEntity.ok(sunService.createSunriseSunset(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SunriseSunsetDto> getSunriseSunsetById(@PathVariable Integer id) {
        SunriseSunsetDto dto = sunService.getSunriseSunsetById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }


    @GetMapping("/all")
    public ResponseEntity<List<SunriseSunsetDto>> getAllSunriseSunsets() {
        return ResponseEntity.ok(sunService.getAllSunriseSunsets());
    }

    @PutMapping("/{id}")
    public ResponseEntity<SunriseSunsetDto> updateSunriseSunset(@PathVariable Integer id, @RequestBody SunriseSunsetDto dto) {
        SunriseSunsetDto updatedDto = sunService.updateSunriseSunset(id, dto);
        return updatedDto != null ? ResponseEntity.ok(updatedDto) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSunriseSunset(@PathVariable Integer id) {
        sunService.deleteSunriseSunset(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/location/{locationId}")
    public ResponseEntity<List<SunriseSunsetDto>> getSunriseSunsetsByLocationId(@PathVariable Integer locationId) {
        return ResponseEntity.ok(sunService.getSunriseSunsetsByLocationId(locationId));
    }

    @GetMapping("/by-date-and-location")
    public ResponseEntity<List<SunriseSunsetDto>> getSunriseSunsetsByDateAndLocation(
            @RequestParam("date") String date,
            @RequestParam("locationName") String locationName) {
        try {
            LocalDate localDate = LocalDate.parse(date);
            return ResponseEntity.ok(sunService.getSunriseSunsetsByDateAndLocationName(localDate, locationName));
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().build();
        }
    }

}