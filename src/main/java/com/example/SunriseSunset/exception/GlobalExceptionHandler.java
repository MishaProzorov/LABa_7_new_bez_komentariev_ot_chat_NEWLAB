package com.example.SunriseSunset.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.request.WebRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final String ERROR_PREFIX = "A %s error (status %d) occurred at %s while accessing %s: %s.";
    private static final String CAUSE_INVALID_DATA = "The request contains invalid %s: %s";
    private static final String SOLUTION_VERIFY = "Verify the %s and correct them according to the API documentation.";
    private static final String INVALID_EXAMPLE = "Invalid: %s";
    private static final String CORRECT_EXAMPLE = "Correct: %s";
    private static final String CAUSE_DATE_FORMAT = "The 'date' parameter could not be parsed because it does not match the expected format: %s";
    private static final String SOLUTION_DATE_FORMAT = "Ensure the 'date' parameter is in the format YYYY-MM-DD.";
    private static final String CAUSE_ILLEGAL_STATE = "The request contains invalid parameters or data: %s";
    private static final String SOLUTION_ILLEGAL_STATE = "Verify the input parameters and correct them according to the API documentation.";
    private static final String CAUSE_UNEXPECTED = "An unexpected error occurred on the server: %s";
    private static final String SOLUTION_UNEXPECTED = "Please try again later or contact the support team with the error details.";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        Map<String, Object> response = new HashMap<>();
        response.put("error", String.format(ERROR_PREFIX, "bad request", HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now(), "", "Validation failed"));
        response.put("cause", String.format(CAUSE_INVALID_DATA, "field values", errors.toString()));
        response.put("solution", String.format(SOLUTION_VERIFY, "input parameters"));
        response.put("invalidExample", String.format(INVALID_EXAMPLE, "latitude=null, longitude=-5"));
        response.put("correctExample", String.format(CORRECT_EXAMPLE, "latitude=48.8566, longitude=2.3522"));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<Object> handleRestClientException(RestClientException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        String path = request.getDescription(false).replace("uri=", "");
        response.put("error", String.format(ERROR_PREFIX, "service unavailable", HttpStatus.SERVICE_UNAVAILABLE.value(),
                LocalDateTime.now(), path, "Failed to fetch data from external API"));
        response.put("cause", String.format(CAUSE_INVALID_DATA, "external API request", ex.getMessage()));
        response.put("solution", "Check your network connection or try again later. If the issue persists, contact the API provider.");
        response.put("invalidExample", String.format(INVALID_EXAMPLE, "Attempting to connect without internet"));
        response.put("correctExample", String.format(CORRECT_EXAMPLE, "Ensure a stable internet connection and retry"));
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        String path = request.getDescription(false).replace("uri=", "");
        response.put("error", String.format(ERROR_PREFIX, "bad request", HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now(), path, ex.getMessage()));
        response.put("cause", String.format(CAUSE_ILLEGAL_STATE, ex.getMessage()));
        response.put("solution", String.format(SOLUTION_ILLEGAL_STATE, "input parameters"));
        response.put("invalidExample", String.format(INVALID_EXAMPLE, "ID = -1"));
        response.put("correctExample", String.format(CORRECT_EXAMPLE, "ID = 1"));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        String path = request.getDescription(false).replace("uri=", "");
        response.put("error", String.format(ERROR_PREFIX, "internal server", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                LocalDateTime.now(), path, "An unexpected error occurred"));
        response.put("cause", String.format(CAUSE_UNEXPECTED, ex.getMessage()));
        response.put("solution", SOLUTION_UNEXPECTED);
        response.put("invalidExample", String.format(INVALID_EXAMPLE, "Unexpected server failure"));
        response.put("correctExample", String.format(CORRECT_EXAMPLE, "Retry after server recovery or contact support"));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}