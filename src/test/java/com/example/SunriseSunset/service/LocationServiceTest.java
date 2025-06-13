package com.example.SunriseSunset.service;

import com.example.SunriseSunset.dto.LocationDto;
import com.example.SunriseSunset.model.LocationEntity;
import com.example.SunriseSunset.model.SunriseSunsetEntity;
import com.example.SunriseSunset.repository.LocationRepository;
import com.example.SunriseSunset.repository.SunriseSunsetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class LocationServiceTest {

    private static final String CACHE_KEY_PREFIX = "Location_";
    private static final String CACHE_KEY_ALL = "Location_All";
    private static final String TEST_CITY = "TestCity";
    private static final String TEST_COUNTRY = "TestCountry";
    private static final List<Integer> TEST_SUNRISE_SUNSET_IDS = List.of(1, 2);

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private SunriseSunsetRepository sunriseSunsetRepository;

    @Mock
    private Map<String, Object> entityCache;

    @Mock
    private LocationDto locationDto;

    @Mock
    private LocationEntity locationEntity;

    @Mock
    private SunriseSunsetEntity sunriseSunsetEntity;

    @InjectMocks
    private LocationService locationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(locationDto.getId()).thenReturn(1);
        when(locationDto.getName()).thenReturn(TEST_CITY);
        when(locationDto.getCountry()).thenReturn(TEST_COUNTRY);
        when(locationDto.getSunriseSunsetIds()).thenReturn(TEST_SUNRISE_SUNSET_IDS);
        when(locationService.convertToDTO(any(LocationEntity.class))).thenReturn(locationDto);
    }

    @Test
    void createLocationWithSunriseSunsetIdsSavesAndCaches() {
        List<SunriseSunsetEntity> sunriseSunsets = List.of(mock(SunriseSunsetEntity.class), mock(SunriseSunsetEntity.class));
        when(sunriseSunsetRepository.findAllById(anyList())).thenReturn(sunriseSunsets);
        when(locationRepository.save(any(LocationEntity.class))).thenReturn(locationEntity);
        LocationDto result = locationService.createLocation(locationDto);
        assertNotNull(result);
        assertEquals(TEST_CITY, result.getName());
        verify(locationRepository).save(any(LocationEntity.class));
        verify(entityCache).put(CACHE_KEY_PREFIX + "1", result);
        verify(entityCache).remove(CACHE_KEY_ALL);
    }

    @Test
    void createLocationWithoutSunriseSunsetIdsSavesAndCaches() {
        when(locationDto.getSunriseSunsetIds()).thenReturn(null);
        when(locationRepository.save(any(LocationEntity.class))).thenReturn(locationEntity);
        LocationDto result = locationService.createLocation(locationDto);
        assertNotNull(result);
        assertEquals(TEST_CITY, result.getName());
        verify(locationRepository).save(any(LocationEntity.class));
        verify(entityCache).put(CACHE_KEY_PREFIX + "1", result);
        verify(entityCache).remove(CACHE_KEY_ALL);
    }

    @Test
    void getLocationByIdCacheHitReturnsCached() {
        when(entityCache.containsKey(CACHE_KEY_PREFIX + "1")).thenReturn(true);
        when(entityCache.get(CACHE_KEY_PREFIX + "1")).thenReturn(locationDto);
        LocationDto result = locationService.getLocationById(1);
        assertNotNull(result);
        assertEquals(TEST_CITY, result.getName());
        verify(entityCache).containsKey(CACHE_KEY_PREFIX + "1");
        verify(entityCache).get(CACHE_KEY_PREFIX + "1");
        verify(locationRepository, never()).findById(anyInt());
    }

    @Test
    void getLocationByIdCacheMissReturnsAndCaches() {
        when(locationRepository.findById(1)).thenReturn(Optional.of(locationEntity));
        LocationDto result = locationService.getLocationById(1);
        assertNotNull(result);
        assertEquals(TEST_CITY, result.getName());
        verify(locationRepository).findById(1);
        verify(entityCache).put(CACHE_KEY_PREFIX + "1", result);
    }

    @Test
    void getLocationByIdNotFoundThrowsException() {
        when(locationRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> locationService.getLocationById(1));
        verify(locationRepository).findById(1);
    }

    @Test
    void getLocationsByIdsReturnsListAndCaches() {
        List<Integer> ids = List.of(1, 2);
        List<LocationEntity> entities = List.of(mock(LocationEntity.class), mock(LocationEntity.class));
        when(locationRepository.findAllById(ids)).thenReturn(entities);
        List<LocationDto> result = locationService.getLocationsByIds(ids);
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(locationRepository).findAllById(ids);
        verify(entityCache).put(CACHE_KEY_PREFIX + "1", locationDto);
        verify(entityCache).put(CACHE_KEY_PREFIX + "2", locationDto);
    }

    @Test
    void getAllLocationsCacheHitReturnsCached() {
        List<LocationDto> cachedList = List.of(locationDto);
        when(entityCache.containsKey(CACHE_KEY_ALL)).thenReturn(true);
        when(entityCache.get(CACHE_KEY_ALL)).thenReturn(cachedList);
        List<LocationDto> result = locationService.getAllLocations();
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(entityCache).containsKey(CACHE_KEY_ALL);
        verify(entityCache).get(CACHE_KEY_ALL);
        verify(locationRepository, never()).findAll();
    }

    @Test
    void getAllLocationsCacheMissReturnsAndCaches() {
        List<LocationEntity> entities = List.of(mock(LocationEntity.class));
        when(locationRepository.findAll()).thenReturn(entities);
        List<LocationDto> result = locationService.getAllLocations();
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(locationRepository).findAll();
        verify(entityCache).put(CACHE_KEY_ALL, result);
        verify(entityCache).put(CACHE_KEY_PREFIX + "0", locationDto);
    }

    @Test
    void updateLocationSuccessfullyUpdatesAndCaches() {
        when(locationRepository.findById(1)).thenReturn(Optional.of(locationEntity));
        when(locationRepository.save(any(LocationEntity.class))).thenReturn(locationEntity);
        LocationDto result = locationService.updateLocation(1, locationDto);
        assertNotNull(result);
        assertEquals(TEST_CITY, result.getName());
        verify(locationRepository).save(any(LocationEntity.class));
        verify(entityCache).put(CACHE_KEY_PREFIX + "1", result);
        verify(entityCache).remove(CACHE_KEY_ALL);
    }

    @Test
    void updateLocationNotFoundThrowsException() {
        when(locationRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> locationService.updateLocation(1, locationDto));
        verify(locationRepository).findById(1);
    }

    @Test
    void deleteLocationSuccessfullyDeletesAndClearsCache() {
        when(locationRepository.existsById(1)).thenReturn(true);
        locationService.deleteLocation(1);
        verify(locationRepository).deleteById(1);
        verify(entityCache).remove(CACHE_KEY_PREFIX + "1");
        verify(entityCache).remove(CACHE_KEY_ALL);
    }

    @Test
    void deleteLocationNotFoundThrowsException() {
        when(locationRepository.existsById(1)).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> locationService.deleteLocation(1));
        verify(locationRepository, never()).deleteById(anyInt());
    }

    @Test
    void bulkCreateLocationsSavesAndCaches() {
        List<LocationDto> dtos = List.of(locationDto);
        List<LocationEntity> entities = List.of(mock(LocationEntity.class));
        when(locationRepository.saveAll(anyList())).thenReturn(entities);
        List<LocationDto> result = locationService.bulkCreateLocations(dtos);
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(locationRepository).saveAll(anyList());
        verify(entityCache).put(CACHE_KEY_PREFIX + "0", locationDto);
        verify(entityCache).remove(CACHE_KEY_ALL);
    }

    @Test
    void bulkDeleteLocationsSuccessfullyDeletesAndClearsCache() {
        List<Integer> ids = List.of(1, 2);
        List<LocationEntity> locations = List.of(mock(LocationEntity.class), mock(LocationEntity.class));
        when(locationRepository.findAllById(ids)).thenReturn(locations);
        when(locationRepository.saveAll(anyList())).thenReturn(locations);
        locationService.bulkDeleteLocations(ids);
        verify(locationRepository).findAllById(ids);
        verify(locationRepository).saveAll(locations);
        verify(locationRepository).deleteAllById(ids);
        verify(entityCache).remove(CACHE_KEY_PREFIX + "1");
        verify(entityCache).remove(CACHE_KEY_PREFIX + "2");
        verify(entityCache).remove(CACHE_KEY_ALL);
    }
}