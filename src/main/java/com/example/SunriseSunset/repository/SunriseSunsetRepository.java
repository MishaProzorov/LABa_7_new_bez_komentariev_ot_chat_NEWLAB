package com.example.SunriseSunset.repository;

import com.example.SunriseSunset.model.SunriseSunsetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

/**Repository interface for managing SunriseSunsetEntity operations.*/
public interface SunriseSunsetRepository extends JpaRepository<SunriseSunsetEntity, Integer> {

    /**Finds sunrise and sunset entities by location ID.*/
    @Query("SELECT s FROM SunriseSunsetEntity s JOIN s.locations l WHERE l.id = :locationId")
    List<SunriseSunsetEntity> findByLocationId(@Param("locationId") Integer locationId);

    /**Finds sunrise and sunset entities by date and location name.*/
    @Query("SELECT s FROM SunriseSunsetEntity s JOIN s.locations l WHERE s.date = :date AND l.name = :locationName")
    List<SunriseSunsetEntity> findByDateAndLocationName(@Param("date") LocalDate date, @Param("locationName") String locationName);
}