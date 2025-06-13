package com.example.SunriseSunset.repository;

import com.example.SunriseSunset.model.LocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**Repository interface for managing LocationEntity operations.*/
public interface LocationRepository extends JpaRepository<LocationEntity, Integer> {
}