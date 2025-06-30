package club.castillo.restaurantes.castillo.service;

import club.castillo.restaurantes.castillo.dto.MenuItemDTO;
import club.castillo.restaurantes.castillo.model.Category;
import club.castillo.restaurantes.castillo.model.MenuItem;
import club.castillo.restaurantes.castillo.model.Restaurant;
import club.castillo.restaurantes.castillo.repository.CategoryRepository;
import club.castillo.restaurantes.castillo.repository.MenuItemRepository;
import club.castillo.restaurantes.castillo.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuItemService {
    private final MenuItemRepository menuItemRepository;
    private final CategoryRepository categoryRepository;
    private final RestaurantRepository restaurantRepository;

    @Transactional(readOnly = true)
    public List<MenuItemDTO> getMenuItemsByRestaurant(Long restaurantId) {
        return menuItemRepository.findByRestaurantIdAndActiveTrue(restaurantId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MenuItemDTO getMenuItemById(Long id) {
        MenuItem menuItem = menuItemRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Plato no encontrado"));
        return toDTO(menuItem);
    }

    @Transactional
    public MenuItemDTO createMenuItem(MenuItemDTO dto) {
        checkRestaurantAdminRole();

        if (dto.getCategoryId() == null) {
            throw new IllegalArgumentException("La categoría es obligatoria");
        }
        if (dto.getRestaurantId() == null) {
            throw new IllegalArgumentException("El restaurante es obligatorio");
        }

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        Restaurant restaurant = restaurantRepository.findById(dto.getRestaurantId())
                .orElseThrow(() -> new RuntimeException("Restaurante no encontrado"));

        MenuItem menuItem = new MenuItem();
        menuItem.setName(dto.getName());
        menuItem.setDescription(dto.getDescription());
        menuItem.setPrice(dto.getPrice());
        menuItem.setCategory(category);
        menuItem.setRestaurant(restaurant);
        menuItem.setImageBase64(dto.getImageBase64());
        menuItem.setActive(dto.isActive());
        menuItem.setAvailable(dto.isAvailable());
        menuItem.setCreatedAt(LocalDateTime.now());

        return toDTO(menuItemRepository.save(menuItem));
    }

    @Transactional
    public MenuItemDTO updateMenuItem(Long id, MenuItemDTO dto) {
        checkRestaurantAdminRole();

        MenuItem menuItem = menuItemRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Plato no encontrado"));
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
            menuItem.setCategory(category);
        }
        if (dto.getRestaurantId() != null) {
            Restaurant restaurant = restaurantRepository.findById(dto.getRestaurantId())
                    .orElseThrow(() -> new RuntimeException("Restaurante no encontrado"));
            menuItem.setRestaurant(restaurant);
        }
        menuItem.setName(dto.getName());
        menuItem.setDescription(dto.getDescription());
        menuItem.setPrice(dto.getPrice());
        menuItem.setImageBase64(dto.getImageBase64());
        menuItem.setActive(dto.isActive());
        menuItem.setAvailable(dto.isAvailable());
        menuItem.setUpdatedAt(LocalDateTime.now());

        return toDTO(menuItemRepository.save(menuItem));
    }

    @Transactional
    public void deleteMenuItem(Long id) {
        checkRestaurantAdminRole();
        MenuItem menuItem = menuItemRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Plato no encontrado"));
        menuItem.setActive(false);
        menuItem.setUpdatedAt(LocalDateTime.now());
        menuItemRepository.save(menuItem);
    }

    private MenuItemDTO toDTO(MenuItem m) {
        return MenuItemDTO.builder()
                .id(m.getId())
                .name(m.getName())
                .description(m.getDescription())
                .price(m.getPrice())
                .categoryId(m.getCategory() != null ? m.getCategory().getId() : null)
                .categoryName(m.getCategory() != null ? m.getCategory().getName() : null)
                .restaurantId(m.getRestaurant() != null ? m.getRestaurant().getId() : null)
                .restaurantName(m.getRestaurant() != null ? m.getRestaurant().getName() : null)
                .imageBase64(m.getImageBase64())
                .active(m.isActive())
                .available(m.isAvailable())
                .build();
    }

    private void checkRestaurantAdminRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        club.castillo.restaurantes.castillo.model.User user = (club.castillo.restaurantes.castillo.model.User) authentication.getPrincipal();
        if (!user.getRole().getName().equals("RESTAURANT_ADMIN") && !user.getRole().getName().equals("OWNER")) {
            throw new AccessDeniedException("Solo RESTAURANT_ADMIN u OWNER pueden gestionar platos");
        }
    }
}
