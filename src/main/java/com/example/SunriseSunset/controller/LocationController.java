package com.example.SunriseSunset.controller;

import com.example.SunriseSunset.dto.LocationDto;
import com.example.SunriseSunset.dto.SunriseSunsetDto;
import com.example.SunriseSunset.service.LocationService;
import com.example.SunriseSunset.service.SunriseSunsetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/locations")
public class LocationController {

    private final LocationService locationService;
    private final SunriseSunsetService sunService;

    @Autowired
    public LocationController(LocationService locationService, SunriseSunsetService sunService) {
        this.locationService = locationService;
        this.sunService = sunService;
    }

    @PostMapping
    public ResponseEntity<LocationDto> createLocation(@RequestBody LocationDto dto) {
        return ResponseEntity.ok(locationService.createLocation(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationDto> getLocationById(@PathVariable Integer id) {
        LocationDto dto = locationService.getLocationById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @GetMapping("/all")
    public ResponseEntity<List<LocationDto>> getAllLocationsApi() {
        return ResponseEntity.ok(locationService.getAllLocations());
    }

    @PutMapping("/{id}")
    public ResponseEntity<LocationDto> updateLocation(@PathVariable Integer id, @RequestBody LocationDto dto) {
        LocationDto updatedDto = locationService.updateLocation(id, dto);
        return updatedDto != null ? ResponseEntity.ok(updatedDto) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLocationApi(@PathVariable Integer id) {
        locationService.deleteLocation(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<LocationDto>> bulkCreateLocations(@RequestBody List<LocationDto> dtos) {
        return ResponseEntity.ok(locationService.bulkCreateLocations(dtos));
    }

    @GetMapping("/list")
    public String listLocations(Model model) {
        List<LocationDto> locations = locationService.getAllLocations();
        model.addAttribute("locations", locations);
        return "locations/list";
    }

    @GetMapping("/new")
    public String newLocationForm(Model model) {
        model.addAttribute("location", new LocationDto());
        model.addAttribute("allSunTimes", sunService.getAllSunriseSunsets());
        return "locations/form";
    }

    @GetMapping("/{id}/view")
    public String viewLocation(@PathVariable Integer id, Model model) {
        LocationDto location = locationService.getLocationById(id);
        List<SunriseSunsetDto> sunTimes = sunService.getSunriseSunsetsByLocationId(id);
        model.addAttribute("location", location);
        model.addAttribute("sunTimes", sunTimes);
        return "locations/view";
    }

    @GetMapping("/{id}/edit")
    public String editLocationForm(@PathVariable Integer id, Model model) {
        LocationDto location = locationService.getLocationById(id);
        model.addAttribute("location", location);
        model.addAttribute("allSunTimes", sunService.getAllSunriseSunsets());
        return "locations/form";
    }

    @PostMapping("/save")
    public String saveLocation(@ModelAttribute LocationDto location) {
        if (location.getId() == null) {
            locationService.createLocation(location);
        } else {
            locationService.updateLocation(location.getId(), location);
        }
        return "redirect:/locations/list";
    }

    @PostMapping("/{id}/delete")
    public String deleteLocation(@PathVariable Integer id) {
        locationService.deleteLocation(id);
        return "redirect:/locations/list";
    }
}