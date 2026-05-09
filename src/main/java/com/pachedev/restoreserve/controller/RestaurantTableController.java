package com.pachedev.restoreserve.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pachedev.restoreserve.dto.RestaurantTableRequestDTO;
import com.pachedev.restoreserve.dto.RestaurantTableResponseDTO;
import com.pachedev.restoreserve.model.enums.TableLocation;
import com.pachedev.restoreserve.model.enums.TableStatus;
import com.pachedev.restoreserve.service.RestaurantTableService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tables")
public class RestaurantTableController {

    private final RestaurantTableService restaurantTableService;

    @PostMapping
    public ResponseEntity<RestaurantTableResponseDTO> create(@Valid @RequestBody RestaurantTableRequestDTO dto) {
        RestaurantTableResponseDTO response = restaurantTableService.create(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantTableResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantTableService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<RestaurantTableResponseDTO>> findAll() {
        return ResponseEntity.ok(restaurantTableService.findAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestaurantTableResponseDTO> update(@PathVariable Long id,
            @Valid @RequestBody RestaurantTableRequestDTO dto) {
        RestaurantTableResponseDTO response = restaurantTableService.update(id, dto);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        restaurantTableService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/capacity/{pax}")
    public ResponseEntity<List<RestaurantTableResponseDTO>> findByMaxPax(@PathVariable Integer pax) {
        return ResponseEntity.ok(restaurantTableService.findByMaxPax(pax));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<RestaurantTableResponseDTO>> findByStatus(@PathVariable TableStatus status) {
        return ResponseEntity.ok(restaurantTableService.findByStatus(status));
    }

    @GetMapping("/location/{location}")
    public ResponseEntity<List<RestaurantTableResponseDTO>> findByLocation(@PathVariable TableLocation location) {
        return ResponseEntity.ok(restaurantTableService.findByLocation(location));
    }

    // '/location-status?location=SALON&status=AVAILABLE'
    @GetMapping("/location-status")
    public ResponseEntity<List<RestaurantTableResponseDTO>> findByLocationAndStatus(
            @RequestParam TableLocation location, @RequestParam TableStatus status) {
        return ResponseEntity.ok(restaurantTableService.findByLocationAndStatus(location, status));
    }
}
