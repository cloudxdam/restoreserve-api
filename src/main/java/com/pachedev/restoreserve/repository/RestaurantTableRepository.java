package com.pachedev.restoreserve.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pachedev.restoreserve.model.entity.RestaurantTable;

public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long> {

    List<RestaurantTable> findByGuests(Integer guests);

    // TODO list by Guests, zone, date...

}
