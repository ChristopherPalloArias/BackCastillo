package club.castillo.restaurantes.castillo.config;

import club.castillo.restaurantes.castillo.model.Restaurant;
import club.castillo.restaurantes.castillo.model.User;
import club.castillo.restaurantes.castillo.repository.RestaurantRepository;
import club.castillo.restaurantes.castillo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("restaurantSecurity")
@RequiredArgsConstructor
public class RestaurantSecurity {
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository; // <--- AGREGA ESTO

    public boolean isOwnRestaurant(Long restaurantId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = null;
        Object principal = authentication.getPrincipal();

        // Spring Security usa UserDetails, saca el email
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
            email = userDetails.getUsername();
        }
        // Fallback (no debería usarse normalmente)
        if (principal instanceof club.castillo.restaurantes.castillo.model.User user) {
            email = user.getEmail();
        }
        if (email == null) return false;

        var realUser = userRepository.findByEmail(email).orElse(null);
        if (realUser == null) return false;

        if ("OWNER".equals(realUser.getRole().getName())) return true;
        Restaurant restaurant = restaurantRepository.findById(restaurantId).orElse(null);
        return restaurant != null && restaurant.getAdmin().getId().equals(realUser.getId());
    }
}

