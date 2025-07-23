package com.example.customer_management_system.repository;

import com.example.customer_management_system.entities.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {

    @Query("SELECT c FROM City c JOIN FETCH c.country")
    List<City> findAllWithCountry();

    @Query("SELECT c FROM City c JOIN FETCH c.country WHERE c.country.id = :countryId")
    List<City> findByCountryIdWithCountry(@Param("countryId") Long countryId);

    Optional<City> findByNameAndCountryId(String name, Long countryId);

    @Query("SELECT c FROM City c JOIN FETCH c.country WHERE c.id = :id")
    Optional<City> findByIdWithCountry(@Param("id") Long id);
}
