package com.example.SunriseSunset.controller;

import com.example.SunriseSunset.service.RequestCounterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/counter")
public class CounterController {

    private final RequestCounterService counterService;

    public CounterController(RequestCounterService counterService) {
        this.counterService = counterService;
    }

    @Operation(summary = "Get request counter value", responses = {
            @ApiResponse(responseCode = "200", description = "Returns current request count")})
    @GetMapping("/request-count")
    public ResponseEntity<Long> getRequestCount() {
        return ResponseEntity.ok(counterService.getCount());
    }

    @Operation(summary = "Reset request counter", responses = {
            @ApiResponse(responseCode = "200", description = "Counter reset successfully")})
    @PostMapping("/reset-counter")
    public ResponseEntity<Void> resetRequestCount() {
        counterService.reset();
        return ResponseEntity.ok().build();
    }
}