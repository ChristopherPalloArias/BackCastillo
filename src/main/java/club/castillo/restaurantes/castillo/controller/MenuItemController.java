package club.castillo.restaurantes.castillo.controller;

import club.castillo.restaurantes.castillo.dto.MenuItemDTO;
import club.castillo.restaurantes.castillo.service.MenuItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/menu-items")
@RequiredArgsConstructor
public class MenuItemController {
    private final MenuItemService menuItemService;

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<MenuItemDTO>> getMenuItemsByRestaurant(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(menuItemService.getMenuItemsByRestaurant(restaurantId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MenuItemDTO> getMenuItemById(@PathVariable Long id) {
        return ResponseEntity.ok(menuItemService.getMenuItemById(id));
    }

    @PreAuthorize("hasRole('RESTAURANT_ADMIN') or hasRole('OWNER')")
    @PostMapping
    public ResponseEntity<MenuItemDTO> createMenuItem(@Valid @RequestBody MenuItemDTO menuItemDTO) {
        return ResponseEntity.ok(menuItemService.createMenuItem(menuItemDTO));
    }

    @PreAuthorize("hasRole('RESTAURANT_ADMIN') or hasRole('OWNER')")
    @PutMapping("/{id}")
    public ResponseEntity<MenuItemDTO> updateMenuItem(@PathVariable Long id, @Valid @RequestBody MenuItemDTO menuItemDTO) {
        return ResponseEntity.ok(menuItemService.updateMenuItem(id, menuItemDTO));
    }

    @PreAuthorize("hasRole('RESTAURANT_ADMIN') or hasRole('OWNER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable Long id) {
        menuItemService.deleteMenuItem(id);
        return ResponseEntity.noContent().build();
    }
}
