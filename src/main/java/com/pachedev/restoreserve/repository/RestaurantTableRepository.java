package com.pachedev.restoreserve.repository;

import com.pachedev.restoreserve.model.entity.RestaurantTable;
import com.pachedev.restoreserve.model.enums.TableLocation;
import com.pachedev.restoreserve.model.enums.TableStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long> {

    List<RestaurantTable> findByMaxPaxGreaterThanEqual(Integer pax);

    List<RestaurantTable> findByStatus(TableStatus status);

    List<RestaurantTable> findByLocation(TableLocation location);

    List<RestaurantTable> findByLocationAndStatus(TableLocation location, TableStatus status);

    List<RestaurantTable> findByStatusAndMaxPaxGreaterThanEqual(TableStatus status, Integer maxPax);

    /**
     * Bloquea la mesa mientras se crea la reserva para evitar que dos reservas
     * simultáneas se realicen sobre la misma mesa.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from RestaurantTable t where t.id = :id")
    Optional<RestaurantTable> findByIdForUpdate(@Param("id") Long id);
}
