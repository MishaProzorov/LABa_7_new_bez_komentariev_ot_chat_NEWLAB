package com.example.SunriseSunset.service;

import com.example.SunriseSunset.dto.SunriseSunsetDto;
import com.example.SunriseSunset.model.LocationEntity;
import com.example.SunriseSunset.model.SunriseSunsetEntity;
import com.example.SunriseSunset.dto.SunriseSunsetModel;
import com.example.SunriseSunset.repository.LocationRepository;
import com.example.SunriseSunset.repository.SunriseSunsetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
public class SunriseSunsetService {

    private static final Logger logger = LoggerFactory.getLogger(SunriseSunsetService.class);
    private static final String SUN_API_URL = "https://api.sunrise-sunset.org/json";
    private static final String CACHE_PREFIX_SUNRISE = "SunriseSunset_";
    private static final String CACHE_KEY_ALL = "SunriseSunset_All";
    private static final String CACHE_KEY_LOCATION = "SunriseSunset_Location_";
    private static final String CACHE_KEY_DATE_LOCATION = "SunriseSunset_Date_";

    private final RestTemplate restTemplate;
    private final SunriseSunsetRepository sunriseSunsetRepository;
    private final LocationRepository locationRepository;
    private final Map<String, Object> entityCache;

    @Autowired
    public SunriseSunsetService(RestTemplate restTemplate,
                                SunriseSunsetRepository sunriseSunsetRepository,
                                LocationRepository locationRepository,
                                Map<String, Object> entityCache) {
        this.restTemplate = restTemplate;
        this.sunriseSunsetRepository = sunriseSunsetRepository;
        this.locationRepository = locationRepository;
        this.entityCache = entityCache;
    }

    public SunriseSunsetDto createSunriseSunset(SunriseSunsetDto dto) {
        SunriseSunsetModel sunData = getSunriseSunset(dto.getLatitude(), dto.getLongitude(), dto.getDate().toString());
        SunriseSunsetEntity entity = new SunriseSunsetEntity();
        entity.date = dto.getDate();
        entity.latitude = dto.getLatitude();
        entity.longitude = dto.getLongitude();
        entity.sunrise = OffsetDateTime.parse(sunData.getResults().getSunrise());
        entity.sunset = OffsetDateTime.parse(sunData.getResults().getSunset());

        if (dto.getLocationIds() != null && !dto.getLocationIds().isEmpty()) {
            entity.locations = locationRepository.findAllById(dto.getLocationIds());
        }

        SunriseSunsetEntity savedEntity = sunriseSunsetRepository.save(entity);
        SunriseSunsetDto savedDto = convertToDTO(savedEntity);


        logger.info("Caching {} {} after creation", CACHE_PREFIX_SUNRISE, savedEntity.id);
        entityCache.put(CACHE_PREFIX_SUNRISE + savedEntity.id, savedDto);
        entityCache.remove(CACHE_KEY_ALL);

        return savedDto;
    }

    public SunriseSunsetDto getSunriseSunsetById(Integer id) {
        String cacheKey = CACHE_PREFIX_SUNRISE + id;
        if (entityCache.containsKey(cacheKey)) {
            logger.debug("Cache hit for {} {}", CACHE_PREFIX_SUNRISE, id);
            return (SunriseSunsetDto) entityCache.get(cacheKey);
        }

        Optional<SunriseSunsetEntity> entity = sunriseSunsetRepository.findById(id);
        if (entity.isPresent()) {
            SunriseSunsetDto dto = convertToDTO(entity.get());
            entityCache.put(cacheKey, dto);
            return dto;
        }
        return null;
    }

    public List<SunriseSunsetDto> getSunriseSunsetsByIds(List<Integer> ids) {
        List<SunriseSunsetEntity> entities = sunriseSunsetRepository.findAllById(ids);
        List<SunriseSunsetDto> dtos = entities.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        dtos.forEach(dto -> entityCache.put(CACHE_PREFIX_SUNRISE + dto.getId(), dto));
        return dtos;
    }

    public List<SunriseSunsetDto> getAllSunriseSunsets() {
        if (entityCache.containsKey(CACHE_KEY_ALL)) {
            logger.debug("Cache hit for all SunriseSunsets");
            return (List<SunriseSunsetDto>) entityCache.get(CACHE_KEY_ALL);
        }

        List<SunriseSunsetEntity> entities = sunriseSunsetRepository.findAll();
        List<SunriseSunsetDto> dtos = entities.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        dtos.forEach(dto -> entityCache.put(CACHE_PREFIX_SUNRISE + dto.getId(), dto));
        entityCache.put(CACHE_KEY_ALL, dtos);

        return dtos;
    }

    public SunriseSunsetDto updateSunriseSunset(Integer id, SunriseSunsetDto dto) {
        Optional<SunriseSunsetEntity> existing = sunriseSunsetRepository.findById(id);
        if (existing.isPresent()) {
            SunriseSunsetEntity entity = existing.get();
            entity.date = dto.getDate();
            entity.latitude = dto.getLatitude();
            entity.longitude = dto.getLongitude();
            SunriseSunsetModel sunData = getSunriseSunset(dto.getLatitude(), dto.getLongitude(), dto.getDate().toString());
            entity.sunrise = OffsetDateTime.parse(sunData.getResults().getSunrise());
            entity.sunset = OffsetDateTime.parse(sunData.getResults().getSunset());

            if (dto.getLocationIds() != null && !dto.getLocationIds().isEmpty()) {
                entity.locations = locationRepository.findAllById(dto.getLocationIds());
            } else {
                entity.locations.clear();
            }

            SunriseSunsetEntity updatedEntity = sunriseSunsetRepository.save(entity);
            SunriseSunsetDto updatedDto = convertToDTO(updatedEntity);
            entityCache.put(CACHE_PREFIX_SUNRISE + id, updatedDto);
            entityCache.remove(CACHE_KEY_ALL);
            return updatedDto;
        }
        return null;
    }

    public void deleteSunriseSunset(Integer id) {
        sunriseSunsetRepository.deleteById(id);
        entityCache.remove(CACHE_PREFIX_SUNRISE + id);
        entityCache.remove(CACHE_KEY_ALL);
    }

    public List<SunriseSunsetDto> getSunriseSunsetsByLocationId(Integer locationId) {
        String cacheKey = CACHE_KEY_LOCATION + locationId;
        if (entityCache.containsKey(cacheKey)) {
            logger.debug("Cache hit for SunriseSunsets by Location ID {}", locationId);
            return (List<SunriseSunsetDto>) entityCache.get(cacheKey);
        }

        List<SunriseSunsetEntity> entities = sunriseSunsetRepository.findByLocationId(locationId);
        List<SunriseSunsetDto> dtos = entities.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        dtos.forEach(dto -> entityCache.put(CACHE_PREFIX_SUNRISE + dto.getId(), dto));
        entityCache.put(cacheKey, dtos);

        return dtos;
    }

    public List<SunriseSunsetDto> getSunriseSunsetsByDateAndLocationName(LocalDate date, String locationName) {
        String cacheKey = CACHE_KEY_DATE_LOCATION + date + "_Location_" + locationName;
        if (entityCache.containsKey(cacheKey)) {
            logger.debug("Cache hit for SunriseSunsets by Date {} and Location {}", date, locationName);
            return (List<SunriseSunsetDto>) entityCache.get(cacheKey);
        }

        List<SunriseSunsetEntity> entities = sunriseSunsetRepository.findByDateAndLocationName(date, locationName);
        List<SunriseSunsetDto> dtos = entities.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        dtos.forEach(dto -> entityCache.put(CACHE_PREFIX_SUNRISE + dto.getId(), dto));
        entityCache.put(cacheKey, dtos);

        return dtos;
    }

    protected SunriseSunsetDto convertToDTO(SunriseSunsetEntity entity) {
        List<Integer> locationIds = entity.locations.stream()
                .map(location -> location.id)
                .collect(Collectors.toList());
        return new SunriseSunsetDto(
                entity.id, entity.date, entity.latitude, entity.longitude,
                entity.sunrise, entity.sunset, locationIds
        );
    }

    private SunriseSunsetModel getSunriseSunset(double lat, double lng, String date) {
        String url = String.format("%s?lat=%f&lng=%f&date=%s&formatted=0", SUN_API_URL, lat, lng, date);
        try {
            return restTemplate.getForObject(url, SunriseSunsetModel.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch sunrise/sunset data: " + e.getMessage());
        }
    }

    public List<SunriseSunsetDto> bulkCreateSunriseSunsets(List<SunriseSunsetDto> dtos) {
        List<SunriseSunsetEntity> entities = new ArrayList<>();
        dtos.forEach(dto -> {
            SunriseSunsetModel sunData = getSunriseSunset(dto.getLatitude(), dto.getLongitude(), dto.getDate().toString());
            SunriseSunsetEntity entity = new SunriseSunsetEntity();
            entity.date = dto.getDate();
            entity.latitude = dto.getLatitude();
            entity.longitude = dto.getLongitude();
            entity.sunrise = OffsetDateTime.parse(sunData.getResults().getSunrise());
            entity.sunset = OffsetDateTime.parse(sunData.getResults().getSunset());
            if (dto.getLocationIds() != null && !dto.getLocationIds().isEmpty()) {
                entity.locations = locationRepository.findAllById(dto.getLocationIds());
            }
            entities.add(entity);
        });

        List<SunriseSunsetEntity> savedEntities = sunriseSunsetRepository.saveAll(entities);
        List<SunriseSunsetDto> savedDtos = savedEntities.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        savedDtos.forEach(dto -> entityCache.put(CACHE_PREFIX_SUNRISE + dto.getId(), dto));
        entityCache.remove(CACHE_KEY_ALL);

        return savedDtos;
    }

}