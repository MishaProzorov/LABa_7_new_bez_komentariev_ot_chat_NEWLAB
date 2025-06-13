package com.example.SunriseSunset.service;

import com.example.SunriseSunset.dto.SunriseSunsetDto;
import com.example.SunriseSunset.dto.Results;
import com.example.SunriseSunset.model.LocationEntity;
import com.example.SunriseSunset.model.SunriseSunsetEntity;
import com.example.SunriseSunset.dto.SunriseSunsetModel;
import com.example.SunriseSunset.repository.LocationRepository;
import com.example.SunriseSunset.repository.SunriseSunsetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class SunriseSunsetServiceTest {

    private static final String CACHE_KEY_PREFIX = "SunriseSunset_";
    private static final String CACHE_KEY_ALL = "SunriseSunset_All";
    private static final String CACHE_KEY_LOCATION = "SunriseSunset_Location_";
    private static final String CACHE_KEY_DATE = "SunriseSunset_Date_";
    private static final double TEST_LATITUDE = 55.7558;
    private static final double TEST_LONGITUDE = 37.6173;
    private static final LocalDate TEST_DATE = LocalDate.now();
    private static final List<Integer> TEST_LOCATION_IDS = List.of(1, 2);
    private static final String TEST_SUNRISE = "2023-01-01T08:00:00+00:00";
    private static final String TEST_SUNSET = "2023-01-01T16:00:00+00:00";
    private static final String TEST_LOCATION_NAME = "TestCity";

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private SunriseSunsetRepository sunriseSunsetRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private Map<String, Object> entityCache;

    @Mock
    private SunriseSunsetDto sunriseSunsetDto;

    @Mock
    private SunriseSunsetEntity sunriseSunsetEntity;

    @Mock
    private LocationEntity locationEntity;

    @Mock
    private SunriseSunsetModel sunriseSunsetModel;

    @Mock
    private Results results;

    @InjectMocks
    private SunriseSunsetService sunriseSunsetService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(sunriseSunsetEntity.id).thenReturn(1);
        when(sunriseSunsetDto.getLatitude()).thenReturn(TEST_LATITUDE);
        when(sunriseSunsetDto.getLongitude()).thenReturn(TEST_LONGITUDE);
        when(sunriseSunsetDto.getDate()).thenReturn(TEST_DATE);
        when(sunriseSunsetDto.getLocationIds()).thenReturn(TEST_LOCATION_IDS);
        when(sunriseSunsetModel.getResults()).thenReturn(results);
        when(results.getSunrise()).thenReturn(TEST_SUNRISE);
        when(results.getSunset()).thenReturn(TEST_SUNSET);
        when(restTemplate.getForObject(anyString(), eq(SunriseSunsetModel.class))).thenReturn(sunriseSunsetModel);
        when(sunriseSunsetRepository.save(any(SunriseSunsetEntity.class))).thenAnswer(invocation -> {
            SunriseSunsetEntity entity = invocation.getArgument(0);
            entity.id = 1;
            return entity;
        });
        when(sunriseSunsetService.convertToDTO(any(SunriseSunsetEntity.class))).thenReturn(sunriseSunsetDto);
    }

    @Test
    void createSunriseSunsetWithLocationIdsSavesAndCaches() {
        List<LocationEntity> locations = List.of(mock(LocationEntity.class), mock(LocationEntity.class));
        when(locationRepository.findAllById(anyList())).thenReturn(locations);
        SunriseSunsetDto result = sunriseSunsetService.createSunriseSunset(sunriseSunsetDto);
        assertNotNull(result);
        assertEquals(1, result.getId());
        verify(restTemplate).getForObject(anyString(), eq(SunriseSunsetModel.class));
        verify(sunriseSunsetRepository).save(any(SunriseSunsetEntity.class));
        verify(entityCache).put(CACHE_KEY_PREFIX + "1", result);
        verify(entityCache).remove(CACHE_KEY_ALL);
    }

    @Test
    void createSunriseSunsetWithoutLocationIdsSavesAndCaches() {
        when(sunriseSunsetDto.getLocationIds()).thenReturn(null);
        SunriseSunsetDto result = sunriseSunsetService.createSunriseSunset(sunriseSunsetDto);
        assertNotNull(result);
        assertEquals(1, result.getId());
        verify(restTemplate).getForObject(anyString(), eq(SunriseSunsetModel.class));
        verify(sunriseSunsetRepository).save(any(SunriseSunsetEntity.class));
        verify(entityCache).put(CACHE_KEY_PREFIX + "1", result);
        verify(entityCache).remove(CACHE_KEY_ALL);
    }

    @Test
    void getSunriseSunsetByIdCacheHitReturnsCached() {
        when(entityCache.containsKey(CACHE_KEY_PREFIX + "1")).thenReturn(true);
        when(entityCache.get(CACHE_KEY_PREFIX + "1")).thenReturn(sunriseSunsetDto);
        SunriseSunsetDto result = sunriseSunsetService.getSunriseSunsetById(1);
        assertNotNull(result);
        assertEquals(TEST_LATITUDE, result.getLatitude());
        verify(entityCache).containsKey(CACHE_KEY_PREFIX + "1");
        verify(entityCache).get(CACHE_KEY_PREFIX + "1");
        verify(sunriseSunsetRepository, never()).findById(anyInt());
    }

    @Test
    void getSunriseSunsetByIdCacheMissReturnsAndCaches() {
        when(sunriseSunsetRepository.findById(1)).thenReturn(Optional.of(sunriseSunsetEntity));
        SunriseSunsetDto result = sunriseSunsetService.getSunriseSunsetById(1);
        assertNotNull(result);
        assertEquals(TEST_LATITUDE, result.getLatitude());
        verify(sunriseSunsetRepository).findById(1);
        verify(entityCache).put(CACHE_KEY_PREFIX + "1", result);
    }

    @Test
    void getSunriseSunsetByIdNotFoundReturnsNull() {
        when(sunriseSunsetRepository.findById(1)).thenReturn(Optional.empty());
        SunriseSunsetDto result = sunriseSunsetService.getSunriseSunsetById(1);
        assertNull(result);
        verify(sunriseSunsetRepository).findById(1);
    }

    @Test
    void getSunriseSunsetsByIdsReturnsListAndCaches() {
        List<Integer> ids = List.of(1, 2);
        List<SunriseSunsetEntity> entities = List.of(mock(SunriseSunsetEntity.class), mock(SunriseSunsetEntity.class));
        when(sunriseSunsetRepository.findAllById(ids)).thenReturn(entities);
        List<SunriseSunsetDto> result = sunriseSunsetService.getSunriseSunsetsByIds(ids);
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(sunriseSunsetRepository).findAllById(ids);
        verify(entityCache).put(CACHE_KEY_PREFIX + "0", sunriseSunsetDto);
        verify(entityCache).put(CACHE_KEY_PREFIX + "1", sunriseSunsetDto);
    }

    @Test
    void getAllSunriseSunsetsCacheHitReturnsCached() {
        List<SunriseSunsetDto> cachedList = List.of(sunriseSunsetDto);
        when(entityCache.containsKey(CACHE_KEY_ALL)).thenReturn(true);
        when(entityCache.get(CACHE_KEY_ALL)).thenReturn(cachedList);
        List<SunriseSunsetDto> result = sunriseSunsetService.getAllSunriseSunsets();
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(entityCache).containsKey(CACHE_KEY_ALL);
        verify(entityCache).get(CACHE_KEY_ALL);
        verify(sunriseSunsetRepository, never()).findAll();
    }

    @Test
    void getAllSunriseSunsetsCacheMissReturnsAndCaches() {
        List<SunriseSunsetEntity> entities = List.of(mock(SunriseSunsetEntity.class));
        when(sunriseSunsetRepository.findAll()).thenReturn(entities);
        List<SunriseSunsetDto> result = sunriseSunsetService.getAllSunriseSunsets();
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(sunriseSunsetRepository).findAll();
        verify(entityCache).put(CACHE_KEY_ALL, result);
        verify(entityCache).put(CACHE_KEY_PREFIX + "0", sunriseSunsetDto);
    }

    @Test
    void updateSunriseSunsetSuccessfullyUpdatesAndCaches() {
        when(sunriseSunsetRepository.findById(1)).thenReturn(Optional.of(sunriseSunsetEntity));
        when(sunriseSunsetRepository.save(any(SunriseSunsetEntity.class))).thenReturn(sunriseSunsetEntity);
        SunriseSunsetDto result = sunriseSunsetService.updateSunriseSunset(1, sunriseSunsetDto);
        assertNotNull(result);
        assertEquals(TEST_LATITUDE, result.getLatitude());
        verify(restTemplate).getForObject(anyString(), eq(SunriseSunsetModel.class));
        verify(sunriseSunsetRepository).save(any(SunriseSunsetEntity.class));
        verify(entityCache).put(CACHE_KEY_PREFIX + "1", result);
        verify(entityCache).remove(CACHE_KEY_ALL);
    }

    @Test
    void updateSunriseSunsetNotFoundReturnsNull() {
        when(sunriseSunsetRepository.findById(1)).thenReturn(Optional.empty());
        SunriseSunsetDto result = sunriseSunsetService.updateSunriseSunset(1, sunriseSunsetDto);
        assertNull(result);
        verify(sunriseSunsetRepository).findById(1);
    }

    @Test
    void deleteSunriseSunsetSuccessfullyDeletesAndClearsCache() {
        doNothing().when(sunriseSunsetRepository).deleteById(1);
        sunriseSunsetService.deleteSunriseSunset(1);
        verify(sunriseSunsetRepository).deleteById(1);
        verify(entityCache).remove(CACHE_KEY_PREFIX + "1");
        verify(entityCache).remove(CACHE_KEY_ALL);
    }

    @Test
    void getSunriseSunsetsByLocationIdCacheHitReturnsCached() {
        List<SunriseSunsetDto> cachedList = List.of(sunriseSunsetDto);
        when(entityCache.containsKey(CACHE_KEY_LOCATION + "1")).thenReturn(true);
        when(entityCache.get(CACHE_KEY_LOCATION + "1")).thenReturn(cachedList);
        List<SunriseSunsetDto> result = sunriseSunsetService.getSunriseSunsetsByLocationId(1);
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(entityCache).containsKey(CACHE_KEY_LOCATION + "1");
        verify(entityCache).get(CACHE_KEY_LOCATION + "1");
        verify(sunriseSunsetRepository, never()).findByLocationId(anyInt());
    }

    @Test
    void getSunriseSunsetsByLocationIdCacheMissReturnsAndCaches() {
        List<SunriseSunsetEntity> entities = List.of(mock(SunriseSunsetEntity.class));
        when(sunriseSunsetRepository.findByLocationId(1)).thenReturn(entities);
        List<SunriseSunsetDto> result = sunriseSunsetService.getSunriseSunsetsByLocationId(1);
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(sunriseSunsetRepository).findByLocationId(1);
        verify(entityCache).put(CACHE_KEY_LOCATION + "1", result);
        verify(entityCache).put(CACHE_KEY_PREFIX + "0", sunriseSunsetDto);
    }

    @Test
    void getSunriseSunsetsByDateAndLocationNameCacheHitReturnsCached() {
        List<SunriseSunsetDto> cachedList = List.of(sunriseSunsetDto);
        when(entityCache.containsKey(CACHE_KEY_DATE + TEST_DATE + "_Location_" + TEST_LOCATION_NAME)).thenReturn(true);
        when(entityCache.get(CACHE_KEY_DATE + TEST_DATE + "_Location_" + TEST_LOCATION_NAME)).thenReturn(cachedList);
        List<SunriseSunsetDto> result = sunriseSunsetService.getSunriseSunsetsByDateAndLocationName(TEST_DATE, TEST_LOCATION_NAME);
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(entityCache).containsKey(CACHE_KEY_DATE + TEST_DATE + "_Location_" + TEST_LOCATION_NAME);
        verify(entityCache).get(CACHE_KEY_DATE + TEST_DATE + "_Location_" + TEST_LOCATION_NAME);
        verify(sunriseSunsetRepository, never()).findByDateAndLocationName(any(), anyString());
    }

    @Test
    void getSunriseSunsetsByDateAndLocationNameCacheMissReturnsAndCaches() {
        List<SunriseSunsetEntity> entities = List.of(mock(SunriseSunsetEntity.class));
        when(sunriseSunsetRepository.findByDateAndLocationName(TEST_DATE, TEST_LOCATION_NAME)).thenReturn(entities);
        List<SunriseSunsetDto> result = sunriseSunsetService.getSunriseSunsetsByDateAndLocationName(TEST_DATE, TEST_LOCATION_NAME);
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(sunriseSunsetRepository).findByDateAndLocationName(TEST_DATE, TEST_LOCATION_NAME);
        verify(entityCache).put(CACHE_KEY_DATE + TEST_DATE + "_Location_" + TEST_LOCATION_NAME, result);
        verify(entityCache).put(CACHE_KEY_PREFIX + "0", sunriseSunsetDto);
    }

    @Test
    void bulkCreateSunriseSunsetsSavesAndCaches() {
        List<SunriseSunsetDto> dtos = List.of(sunriseSunsetDto);
        List<LocationEntity> locations = List.of(mock(LocationEntity.class));
        when(locationRepository.findAllById(anyList())).thenReturn(locations);
        List<SunriseSunsetEntity> entities = List.of(mock(SunriseSunsetEntity.class));
        when(sunriseSunsetRepository.saveAll(anyList())).thenReturn(entities);
        List<SunriseSunsetDto> result = sunriseSunsetService.bulkCreateSunriseSunsets(dtos);
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(restTemplate).getForObject(anyString(), eq(SunriseSunsetModel.class));
        verify(sunriseSunsetRepository).saveAll(anyList());
        verify(entityCache).put(CACHE_KEY_PREFIX + "0", sunriseSunsetDto);
        verify(entityCache).remove(CACHE_KEY_ALL);
    }

    @Test
    void bulkDeleteSunriseSunsetsSuccessfullyDeletesAndClearsCache() {
        List<Integer> ids = List.of(1, 2);
        List<SunriseSunsetEntity> entities = List.of(mock(SunriseSunsetEntity.class), mock(SunriseSunsetEntity.class));
        when(sunriseSunsetRepository.findAllById(ids)).thenReturn(entities);
        when(sunriseSunsetRepository.saveAll(anyList())).thenReturn(entities);
        verify(sunriseSunsetRepository).findAllById(ids);
        verify(sunriseSunsetRepository).saveAll(entities);
        verify(sunriseSunsetRepository).deleteAllById(ids);
        verify(entityCache).remove(CACHE_KEY_PREFIX + "1");
        verify(entityCache).remove(CACHE_KEY_PREFIX + "2");
        verify(entityCache).remove(CACHE_KEY_ALL);
    }
}