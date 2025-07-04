package club.castillo.restaurantes.castillo.controller;

import club.castillo.restaurantes.castillo.dto.BeverageDTO;
import club.castillo.restaurantes.castillo.model.Beverage;
import club.castillo.restaurantes.castillo.model.Restaurant;
import club.castillo.restaurantes.castillo.model.RestaurantBeverage;
import club.castillo.restaurantes.castillo.model.RestaurantBeverageId;
import club.castillo.restaurantes.castillo.repository.BeverageRepository;
import club.castillo.restaurantes.castillo.repository.RestaurantBeverageRepository;
import club.castillo.restaurantes.castillo.repository.RestaurantRepository;
import club.castillo.restaurantes.castillo.service.BeverageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/beverages")
@RequiredArgsConstructor
public class BeverageController {

    private final BeverageService beverageService;
    private final RestaurantBeverageRepository restaurantBeverageRepository;
    private final RestaurantRepository restaurantRepository;
    private final BeverageRepository beverageRepository;

    // --- ENDPOINTS OWNER ---
    @GetMapping
    public ResponseEntity<List<BeverageDTO>> getAllBeverages() {
        return ResponseEntity.ok(beverageService.getAllBeverages());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BeverageDTO> getBeverageById(@PathVariable Long id) {
        return ResponseEntity.ok(beverageService.getBeverageById(id));
    }

    @PostMapping
    public ResponseEntity<BeverageDTO> createBeverage(@Valid @RequestBody BeverageDTO beverageDTO) {
        try {
            log.info("Recibiendo petición para crear bebida: {}", beverageDTO);
            log.info("CategoryId en controller: {}", beverageDTO.getCategoryId());

            BeverageDTO createdBeverage = beverageService.createBeverage(beverageDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdBeverage);
        } catch (IllegalArgumentException e) {
            log.error("Error de validación: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error inesperado al crear bebida", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<BeverageDTO> updateBeverage(@PathVariable Long id, @Valid @RequestBody BeverageDTO beverageDTO) {
        try {
            return ResponseEntity.ok(beverageService.updateBeverage(id, beverageDTO));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBeverage(@PathVariable Long id) {
        try {
            beverageService.deleteBeverage(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // --- ENDPOINT PÚBLICO: listado de bebidas por restaurante ---
    @GetMapping("/restaurant/{restaurantId}")
    public List<BeverageDTO> getBeveragesForRestaurant(@PathVariable Long restaurantId) {
        List<Beverage> globalBeverages = beverageRepository.findByActiveTrueAndAvailableTrue();
        List<RestaurantBeverage> rels = restaurantBeverageRepository.findByRestaurantId(restaurantId);

        Map<Long, Boolean> localState = rels.stream()
                .collect(Collectors.toMap(
                        rb -> rb.getBeverage().getId(),
                        RestaurantBeverage::isAvailable
                ));

        return globalBeverages.stream()
                .map(b -> {
                    boolean available = localState.getOrDefault(b.getId(), true);
                    return BeverageDTO.builder()
                            .id(b.getId())
                            .name(b.getName())
                            .description(b.getDescription())
                            .price(b.getPrice())
                            .categoryId(b.getCategory() != null ? b.getCategory().getId() : null)
                            .categoryName(b.getCategory() != null ? b.getCategory().getName() : null)
                            .imageBase64(b.getImageBase64())
                            .available(available)
                            .build();
                })
                .collect(Collectors.toList());
    }

    // --- Cambiar disponibilidad local de bebida por restaurante ---
    @PutMapping("/restaurant/{restaurantId}/{beverageId}/available")
    public ResponseEntity<Void> updateAvailability(
            @PathVariable Long restaurantId,
            @PathVariable Long beverageId,
            @RequestParam boolean available
    ) {
        RestaurantBeverage rel = restaurantBeverageRepository
                .findByRestaurantIdAndBeverageId(restaurantId, beverageId)
                .orElseGet(() -> {
                    Restaurant restaurant = restaurantRepository.findById(restaurantId)
                            .orElseThrow(() -> new RuntimeException("Restaurante no existe"));
                    Beverage beverage = beverageRepository.findById(beverageId)
                            .orElseThrow(() -> new RuntimeException("Bebida no existe"));
                    return RestaurantBeverage.builder()
                            .id(new RestaurantBeverageId(restaurantId, beverageId))
                            .restaurant(restaurant)
                            .beverage(beverage)
                            .available(true)
                            .build();
                });

        rel.setAvailable(available);
        restaurantBeverageRepository.save(rel);
        return ResponseEntity.ok().build();
    }
}
