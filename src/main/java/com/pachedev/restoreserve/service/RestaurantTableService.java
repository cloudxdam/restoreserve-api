package com.pachedev.restoreserve.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.pachedev.restoreserve.dto.RestaurantTableRequestDTO;
import com.pachedev.restoreserve.dto.RestaurantTableResponseDTO;
import com.pachedev.restoreserve.exception.ResourceNotFoundException;
import com.pachedev.restoreserve.model.entity.RestaurantTable;
import com.pachedev.restoreserve.model.enums.TableLocation;
import com.pachedev.restoreserve.model.enums.TableStatus;
import com.pachedev.restoreserve.repository.RestaurantTableRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RestaurantTableService {

    private final RestaurantTableRepository restaurantTableRepository;

    /**
     * Crea una nueva mesa con estado inicial AVAILABLE.
     */
    public RestaurantTableResponseDTO create(RestaurantTableRequestDTO dto) {

        RestaurantTable table = new RestaurantTable();

        table.setName(dto.name());
        table.setMaxPax(dto.maxPax());
        table.setStatus(TableStatus.AVAILABLE);
        table.setLocation(dto.location());

        RestaurantTable savedTable = restaurantTableRepository.save(table);

        return toResponseDTO(savedTable);
    }

    /**
     * Busca una mesa por su identificador.
     */
    public RestaurantTableResponseDTO findById(Long id) {
        RestaurantTable table = getTableById(id);

        return toResponseDTO(table);
    }

    private RestaurantTable getTableById(Long id) {
        RestaurantTable table = restaurantTableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mesa con id " + id + " no encontrada"));
        return table;
    }

    /**
     * Devuelve la lista completa de mesas registradas.
     */
    public List<RestaurantTableResponseDTO> findAll() {
        List<RestaurantTable> tables = restaurantTableRepository.findAll();
        List<RestaurantTableResponseDTO> response = toResponseList(tables);

        return response;
    }

    private List<RestaurantTableResponseDTO> toResponseList(List<RestaurantTable> tables) {
        List<RestaurantTableResponseDTO> response = new ArrayList<>();

        for (RestaurantTable r : tables) {
            response.add(toResponseDTO(r));
        }
        return response;
    }

    /**
     * Convierte una entidad a un DTO de respuesta.
     */
    public RestaurantTableResponseDTO toResponseDTO(RestaurantTable table) {
        return new RestaurantTableResponseDTO(table.getId(), table.getName(), table.getMaxPax(), table.getStatus(),
                table.getLocation());
    }

    /**
     * Actualiza los datos principales de una mesa existente.
     */
    public RestaurantTableResponseDTO update(Long id, RestaurantTableRequestDTO dto) {
        RestaurantTable table = getTableById(id);

        table.setName(dto.name());
        table.setMaxPax(dto.maxPax());
        table.setLocation(dto.location());

        RestaurantTable updatedTable = restaurantTableRepository.save(table);

        return toResponseDTO(updatedTable);
    }

    public void delete(Long id) {
        restaurantTableRepository.delete(getTableById(id));
    }

    /**
     * Devuelve las mesas cuya capacidad es mayor o igual al número de comensales
     * indicado.
     */
    public List<RestaurantTableResponseDTO> findByMaxPax(Integer pax) {
        List<RestaurantTable> tables = restaurantTableRepository.findByMaxPaxGreaterThanEqual(pax);
        List<RestaurantTableResponseDTO> response = toResponseList(tables);

        return response;
    }

    /**
     * Devuelve las mesas filtrando por estado.
     */
    public List<RestaurantTableResponseDTO> findByStatus(TableStatus status) {
        List<RestaurantTable> tables = restaurantTableRepository.findByStatus(status);
        List<RestaurantTableResponseDTO> response = toResponseList(tables);

        return response;
    }

    /**
     * Devuelve las mesas filtrando por ubicación.
     */
    public List<RestaurantTableResponseDTO> findByLocation(TableLocation location) {
        List<RestaurantTable> tables = restaurantTableRepository.findByLocation(location);
        List<RestaurantTableResponseDTO> response = toResponseList(tables);

        return response;
    }

    /**
     * Devuelve las mesas filtrando por ubicación y estado.
     */
    public List<RestaurantTableResponseDTO> findByLocationAndStatus(TableLocation location, TableStatus status) {
        List<RestaurantTable> tables = restaurantTableRepository.findByLocationAndStatus(location, status);
        List<RestaurantTableResponseDTO> response = toResponseList(tables);

        return response;
    }

    /**
     * Devuelve las mesas filtrando por estado y capacidad.
     */
    public List<RestaurantTableResponseDTO> findByStatusAndMaxPaxGreaterThanEqual(TableStatus status, Integer maxPax) {
        List<RestaurantTable> tables = restaurantTableRepository.findByStatusAndMaxPaxGreaterThanEqual(status, maxPax);
        List<RestaurantTableResponseDTO> response = toResponseList(tables);

        return response;
    }
}
