package com.example.SunriseSunset.service;

import com.example.SunriseSunset.dto.LocationDto;
import com.example.SunriseSunset.model.LocationEntity;
import com.example.SunriseSunset.repository.LocationRepository;
import com.example.SunriseSunset.repository.SunriseSunsetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
@SuppressWarnings("unchecked")
public class LocationService {

    private static final Logger logger = LoggerFactory.getLogger(LocationService.class);
    private static final String CACHE_PREFIX_LOCATION = "Location_";
    private static final String CACHE_KEY_ALL = "Location_All";

    private final LocationRepository locationRepository;
    private final SunriseSunsetRepository sunriseSunsetRepository;
    private final Map<String, Object> entityCache;
    private final RequestCounterService requestCounterService;

    @Autowired
    public LocationService(LocationRepository locationRepository,
                           SunriseSunsetRepository sunriseSunsetRepository,
                           Map<String, Object> entityCache) {
        this.locationRepository = locationRepository;
        this.sunriseSunsetRepository = sunriseSunsetRepository;
        this.entityCache = entityCache;
        this.requestCounterService = new RequestCounterService();
    }

    public LocationDto createLocation(LocationDto dto) {
        requestCounterService.increment();
        LocationEntity entity = new LocationEntity();
        entity.name = dto.getName();
        entity.country = dto.getCountry();

        if (dto.getSunriseSunsetIds() != null && !dto.getSunriseSunsetIds().isEmpty()) {
            entity.sunriseSunsets = sunriseSunsetRepository.findAllById(dto.getSunriseSunsetIds());
        }

        LocationEntity savedEntity = locationRepository.save(entity);
        LocationDto savedDto = convertToDTO(savedEntity);

        logger.info("Caching {} {} after creation", CACHE_PREFIX_LOCATION, savedEntity.id);
        entityCache.put(CACHE_PREFIX_LOCATION + savedEntity.id, savedDto);
        entityCache.remove(CACHE_KEY_ALL);

        return savedDto;
    }

    public LocationDto getLocationById(Integer id) {
        requestCounterService.increment();
        String cacheKey = CACHE_PREFIX_LOCATION + id;
        if (entityCache.containsKey(cacheKey)) {
            logger.debug("Cache hit for {} {}", CACHE_PREFIX_LOCATION, id);
            return (LocationDto) entityCache.get(cacheKey);
        }

        logger.debug("Cache miss for {} {}, querying database", CACHE_PREFIX_LOCATION, id);
        LocationEntity entity = locationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Location not found with id: " + id));

        LocationDto dto = convertToDTO(entity);
        entityCache.put(cacheKey, dto);

        return dto;
    }

    public List<LocationDto> getLocationsByIds(List<Integer> ids) {
        requestCounterService.increment();
        List<LocationEntity> entities = locationRepository.findAllById(ids);
        List<LocationDto> dtos = entities.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        dtos.forEach(dto -> entityCache.put(CACHE_PREFIX_LOCATION + dto.getId(), dto));
        return dtos;
    }

    public List<LocationDto> getAllLocations() {
        requestCounterService.increment();
        if (entityCache.containsKey(CACHE_KEY_ALL)) {
            logger.debug("Cache hit for all Locations");
            return (List<LocationDto>) entityCache.get(CACHE_KEY_ALL);
        }

        logger.debug("Cache miss for all Locations, querying database");
        List<LocationEntity> entities = locationRepository.findAll();
        List<LocationDto> dtos = entities.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        dtos.forEach(dto -> entityCache.put(CACHE_PREFIX_LOCATION + dto.getId(), dto));
        entityCache.put(CACHE_KEY_ALL, dtos);

        return dtos;
    }

    public LocationDto updateLocation(Integer id, LocationDto dto) {
        requestCounterService.increment();
        LocationEntity entity = locationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Location not found with id: " + id));

        entity.name = dto.getName();
        entity.country = dto.getCountry();

        if (dto.getSunriseSunsetIds() != null && !dto.getSunriseSunsetIds().isEmpty()) {
            entity.sunriseSunsets = sunriseSunsetRepository.findAllById(dto.getSunriseSunsetIds());
        } else {
            entity.sunriseSunsets.clear();
        }

        LocationEntity updatedEntity = locationRepository.save(entity);
        LocationDto updatedDto = convertToDTO(updatedEntity);

        entityCache.put(CACHE_PREFIX_LOCATION + id, updatedDto);
        entityCache.remove(CACHE_KEY_ALL);

        return updatedDto;
    }

    public void deleteLocation(Integer id) {
        requestCounterService.increment();
        if (!locationRepository.existsById(id)) {
            throw new IllegalArgumentException("Location not found with id: " + id);
        }

        locationRepository.deleteById(id);
        entityCache.remove(CACHE_PREFIX_LOCATION + id);
        entityCache.remove(CACHE_KEY_ALL);
    }

    protected LocationDto convertToDTO(LocationEntity entity) {
        List<Integer> sunriseSunsetIds = entity.sunriseSunsets.stream()
                .map(sunriseSunset -> sunriseSunset.id)
                .collect(Collectors.toList());
        return new LocationDto(entity.id, entity.name, entity.country, sunriseSunsetIds);
    }

    public List<LocationDto> bulkCreateLocations(List<LocationDto> dtos) {
        requestCounterService.increment();
        List<LocationEntity> entities = new ArrayList<>();
        dtos.forEach(dto -> {
            LocationEntity entity = new LocationEntity();
            entity.name = dto.getName();
            entity.country = dto.getCountry();
            if (dto.getSunriseSunsetIds() != null && !dto.getSunriseSunsetIds().isEmpty()) {
                entity.sunriseSunsets = sunriseSunsetRepository.findAllById(dto.getSunriseSunsetIds());
            }
            entities.add(entity);
        });

        List<LocationEntity> savedEntities = locationRepository.saveAll(entities);
        List<LocationDto> savedDtos = savedEntities.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        savedDtos.forEach(dto -> entityCache.put(CACHE_PREFIX_LOCATION + dto.getId(), dto));
        entityCache.remove(CACHE_KEY_ALL);

        return savedDtos;
    }

    public void bulkDeleteLocations(List<Integer> ids) {
        requestCounterService.increment();
        List<LocationEntity> locations = locationRepository.findAllById(ids);
        locations.forEach(location -> {
            location.sunriseSunsets.forEach(ss -> ss.locations.remove(location));
            location.sunriseSunsets.clear();
        });
        locationRepository.saveAll(locations);
        locationRepository.deleteAllById(ids);
        ids.forEach(id -> entityCache.remove(CACHE_PREFIX_LOCATION + id));
        entityCache.remove(CACHE_KEY_ALL);
    }

    public long getRequestCount() {
        return requestCounterService.getCount();
    }

    public void resetRequestCount() {
        requestCounterService.reset();
    }
}