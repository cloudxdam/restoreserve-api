package com.pachedev.restoreserve.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pachedev.restoreserve.model.entity.RestaurantTable;
import com.pachedev.restoreserve.model.enums.TableLocation;
import com.pachedev.restoreserve.model.enums.TableStatus;

public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long> {

    List<RestaurantTable> findByMaxPax(Integer pax);

    List<RestaurantTable> findByStatus(TableStatus status);

    List<RestaurantTable> findByLocation(TableLocation location);

    List<RestaurantTable> findByLocationAndStatus(TableLocation location, TableStatus status);

    

}
