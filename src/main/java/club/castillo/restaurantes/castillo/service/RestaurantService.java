package club.castillo.restaurantes.castillo.service;

import club.castillo.restaurantes.castillo.dto.RestaurantDTO;
import club.castillo.restaurantes.castillo.model.Restaurant;
import club.castillo.restaurantes.castillo.model.RestaurantStatus;
import club.castillo.restaurantes.castillo.model.RoleType;
import club.castillo.restaurantes.castillo.model.User;
import club.castillo.restaurantes.castillo.model.Zone;
import club.castillo.restaurantes.castillo.repository.RestaurantRepository;
import club.castillo.restaurantes.castillo.repository.UserRepository;
import club.castillo.restaurantes.castillo.repository.ZoneRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestaurantService {
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final ZoneRepository zoneRepository;

    @Transactional(readOnly = true)
    public List<RestaurantDTO> getAllRestaurants() {
        return restaurantRepository.findByActiveTrue()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RestaurantDTO getRestaurantById(Long id) {
        Restaurant restaurant = restaurantRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));
        return convertToDTO(restaurant);
    }

    @Transactional
    public RestaurantDTO createRestaurant(RestaurantDTO restaurantDTO) {
        User admin = userRepository.findById(restaurantDTO.getAdminId())
                .orElseThrow(() -> new RuntimeException("Admin user not found"));

        if (!admin.getRole().getName().equals(RoleType.RESTAURANT_ADMIN)) {
            throw new RuntimeException("User must be a RESTAURANT_ADMIN");
        }

        var zone = zoneRepository.findById(restaurantDTO.getZoneId())
                .orElseThrow(() -> new IllegalArgumentException("Zone not found"));

        Restaurant restaurant = Restaurant.builder()
                .name(restaurantDTO.getName())
                .description(restaurantDTO.getDescription())
                .admin(admin)
                .zone(zone)
                .status(RestaurantStatus.CLOSED)
                .active(true)
                .imageBase64(restaurantDTO.getImageBase64())
                .build();

        restaurant = restaurantRepository.save(restaurant);
        return convertToDTO(restaurant);
    }

    @Transactional
    public RestaurantDTO updateRestaurant(Long id, RestaurantDTO restaurantDTO) {
        Restaurant restaurant = restaurantRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        if (currentUser.getRole().getName().equals(RoleType.RESTAURANT_ADMIN) && 
            !currentUser.getId().equals(restaurant.getAdmin().getId())) {
            throw new AccessDeniedException("You can only update your own restaurant");
        }

        if (restaurantDTO.getAdminId() != null) {
            User newAdmin = userRepository.findById(restaurantDTO.getAdminId())
                    .orElseThrow(() -> new RuntimeException("Admin user not found"));

            if (!newAdmin.getRole().getName().equals(RoleType.RESTAURANT_ADMIN)) {
                throw new RuntimeException("User must be a RESTAURANT_ADMIN");
            }
            restaurant.setAdmin(newAdmin);
        }

        if (restaurantDTO.getZoneId() != null && !restaurantDTO.getZoneId().equals(restaurant.getZone().getId())) {
            var newZone = zoneRepository.findById(restaurantDTO.getZoneId())
                    .orElseThrow(() -> new IllegalArgumentException("Zone not found"));
            restaurant.setZone(newZone);
        }

        restaurant.setName(restaurantDTO.getName());
        restaurant.setDescription(restaurantDTO.getDescription());
        restaurant.setStatus(RestaurantStatus.valueOf(restaurantDTO.getStatus()));
        restaurant.setUpdatedAt(LocalDateTime.now());
        if (restaurantDTO.getImageBase64() != null) {
            restaurant.setImageBase64(restaurantDTO.getImageBase64()); // <-- AGREGADO
        }
        restaurant = restaurantRepository.save(restaurant);
        return convertToDTO(restaurant);
    }

    @Transactional
    public void deleteRestaurant(Long id) {
        Restaurant restaurant = restaurantRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        if (currentUser.getRole().getName().equals(RoleType.OWNER)) {
            restaurant.setActive(false);
            restaurant.setUpdatedAt(LocalDateTime.now());
            restaurantRepository.save(restaurant);
        } else if (currentUser.getRole().getName().equals(RoleType.RESTAURANT_ADMIN) && 
                   currentUser.getId().equals(restaurant.getAdmin().getId())) {
            restaurant.setActive(false);
            restaurant.setUpdatedAt(LocalDateTime.now());
            restaurantRepository.save(restaurant);
        } else {
            throw new AccessDeniedException("You don't have permission to delete this restaurant");
        }
    }

    private RestaurantDTO convertToDTO(Restaurant restaurant) {
        return RestaurantDTO.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .description(restaurant.getDescription())
                .adminId(restaurant.getAdmin().getId())
                .zoneId(restaurant.getZone().getId())
                .status(restaurant.getStatus().name())
                .active(restaurant.isActive())
                .imageBase64(restaurant.getImageBase64())
                .build();
    }
} 