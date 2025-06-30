package club.castillo.restaurantes.castillo.controller;

import club.castillo.restaurantes.castillo.dto.RestaurantDTO;
import club.castillo.restaurantes.castillo.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/restaurants")
@RequiredArgsConstructor
public class RestaurantController {
    private final RestaurantService restaurantService;

    // El OWNER puede ver todos los restaurantes
    @PreAuthorize("hasRole('OWNER')")
    @GetMapping
    public ResponseEntity<List<RestaurantDTO>> getAllRestaurants() {
        return ResponseEntity.ok(restaurantService.getAllRestaurants());
    }
    // --- GET PÚBLICO: Listado de restaurantes para menú, QR, etc ---
    @GetMapping("/public")
    public ResponseEntity<List<RestaurantDTO>> getAllRestaurantsPublic() {
        return ResponseEntity.ok(restaurantService.getAllRestaurants());
    }

    // OWNER puede ver cualquiera, RESTAURANT_ADMIN solo el suyo
    @PreAuthorize("hasRole('OWNER') or (hasRole('RESTAURANT_ADMIN') and @restaurantSecurity.isOwnRestaurant(#id))")
    @GetMapping("/{id}")
    public ResponseEntity<RestaurantDTO> getRestaurantById(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.getRestaurantById(id));
    }

    // --- GET PÚBLICO, SIN RESTRICCIÓN ---
    @GetMapping("/public/{id}")
    public ResponseEntity<RestaurantDTO> getRestaurantByIdPublic(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.getRestaurantById(id));
    }

    // Solo OWNER puede crear restaurantes (por lógica de negocio)
    @PreAuthorize("hasRole('OWNER')")
    @PostMapping
    public ResponseEntity<RestaurantDTO> createRestaurant(@RequestBody RestaurantDTO restaurantDTO) {
        return ResponseEntity.ok(restaurantService.createRestaurant(restaurantDTO));
    }

    // OWNER puede editar cualquier restaurante, RESTAURANT_ADMIN solo el suyo
    @PreAuthorize("hasRole('OWNER') or (hasRole('RESTAURANT_ADMIN') and @restaurantSecurity.isOwnRestaurant(#id))")
    @PutMapping("/{id}")
    public ResponseEntity<RestaurantDTO> updateRestaurant(@PathVariable Long id, @RequestBody RestaurantDTO restaurantDTO) {
        return ResponseEntity.ok(restaurantService.updateRestaurant(id, restaurantDTO));
    }

    // Solo OWNER puede eliminar restaurantes
    @PreAuthorize("hasRole('OWNER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRestaurant(@PathVariable Long id) {
        restaurantService.deleteRestaurant(id);
        return ResponseEntity.ok().build();
    }
}
