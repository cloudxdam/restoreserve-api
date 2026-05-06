package com.pachedev.restoreserve.dto;

import com.pachedev.restoreserve.model.enums.TableLocation;
import com.pachedev.restoreserve.model.enums.TableStatus;

public record RestaurantTableResponseDTO(
        Long id,
        String name,
        Integer maxPax,
        TableStatus status,
        TableLocation location) {

}