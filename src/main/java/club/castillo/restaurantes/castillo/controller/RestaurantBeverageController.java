package club.castillo.restaurantes.castillo.controller;

import club.castillo.restaurantes.castillo.dto.RestaurantBeverageDTO;
import club.castillo.restaurantes.castillo.model.Beverage;
import club.castillo.restaurantes.castillo.model.Restaurant;
import club.castillo.restaurantes.castillo.model.RestaurantBeverage;
import club.castillo.restaurantes.castillo.model.RestaurantBeverageId;
import club.castillo.restaurantes.castillo.repository.BeverageRepository;
import club.castillo.restaurantes.castillo.repository.RestaurantBeverageRepository;
import club.castillo.restaurantes.castillo.repository.RestaurantRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/restaurant-beverages")
@RequiredArgsConstructor
public class RestaurantBeverageController {
    private final RestaurantBeverageRepository restaurantBeverageRepository;
    private final RestaurantRepository restaurantRepository;
    private final BeverageRepository beverageRepository;

    // Listar bebidas propias del restaurante (menú del restaurante)
    @GetMapping("/{restaurantId}")
    public List<RestaurantBeverageDTO> getRestaurantBeverages(@PathVariable Long restaurantId) {
        return restaurantBeverageRepository.findByRestaurantId(restaurantId)
                .stream()
                .map(rb -> RestaurantBeverageDTO.builder()
                        .restaurantId(rb.getRestaurant().getId())
                        .beverageId(rb.getBeverage().getId())
                        .available(rb.isAvailable())
                        .beverageName(rb.getBeverage().getName())
                        .beverageDescription(rb.getBeverage().getDescription())
                        .beveragePrice(rb.getBeverage().getPrice())
                        .beverageImageBase64(rb.getBeverage().getImageBase64())
                        .beverageCategoryId(
                                rb.getBeverage().getCategory() != null
                                        ? rb.getBeverage().getCategory().getId()
                                        : null
                        )
                        .beverageCategoryName(
                                rb.getBeverage().getCategory() != null
                                        ? rb.getBeverage().getCategory().getName()
                                        : null
                        )
                        .build())
                .collect(Collectors.toList());
    }

    // Añadir bebida al menú del restaurante
    @PreAuthorize("hasRole('RESTAURANT_ADMIN') or hasRole('OWNER')")
    @PostMapping("/{restaurantId}/{beverageId}")
    public ResponseEntity<Void> addBeverageToRestaurant(@PathVariable Long restaurantId, @PathVariable Long beverageId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurante no existe"));
        Beverage beverage = beverageRepository.findById(beverageId)
                .orElseThrow(() -> new RuntimeException("Bebida no existe"));

        // Solo permite agregar bebidas activas y disponibles globalmente
        if (!Boolean.TRUE.equals(beverage.getActive()) || !Boolean.TRUE.equals(beverage.getAvailable())) {
            return ResponseEntity.badRequest().build();
        }

        RestaurantBeverageId id = new RestaurantBeverageId(restaurantId, beverageId);
        if (restaurantBeverageRepository.existsById(id)) {
            return ResponseEntity.status(409).build(); // ya existe
        }

        RestaurantBeverage rel = RestaurantBeverage.builder()
                .id(id)
                .restaurant(restaurant)
                .beverage(beverage)
                .available(true)
                .build();

        restaurantBeverageRepository.save(rel);
        return ResponseEntity.ok().build();
    }

    // Quitar bebida del menú del restaurante
    @PreAuthorize("hasRole('RESTAURANT_ADMIN') or hasRole('OWNER')")
    @DeleteMapping("/{restaurantId}/{beverageId}")
    public ResponseEntity<Void> removeBeverageFromRestaurant(@PathVariable Long restaurantId, @PathVariable Long beverageId) {
        RestaurantBeverageId id = new RestaurantBeverageId(restaurantId, beverageId);
        if (restaurantBeverageRepository.existsById(id)) {
            restaurantBeverageRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Cambiar disponibilidad local
    @PreAuthorize("hasRole('RESTAURANT_ADMIN') or hasRole('OWNER')")
    @PutMapping("/{restaurantId}/{beverageId}/available")
    public ResponseEntity<Void> updateAvailability(
            @PathVariable Long restaurantId,
            @PathVariable Long beverageId,
            @RequestParam boolean available
    ) {
        RestaurantBeverage rel = restaurantBeverageRepository
                .findByRestaurantIdAndBeverageId(restaurantId, beverageId)
                .orElseThrow(() -> new RuntimeException("No existe la bebida en el menú del restaurante"));
        rel.setAvailable(available);
        restaurantBeverageRepository.save(rel);
        return ResponseEntity.ok().build();
    }
}
