package club.castillo.restaurantes.castillo.repository;

import club.castillo.restaurantes.castillo.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByRestaurantIdAndActiveTrue(Long restaurantId);
    Optional<MenuItem> findByIdAndActiveTrue(Long id);
}
