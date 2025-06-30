package club.castillo.restaurantes.castillo.repository;

import club.castillo.restaurantes.castillo.model.RestaurantBeverage;
import club.castillo.restaurantes.castillo.model.RestaurantBeverageId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RestaurantBeverageRepository extends JpaRepository<RestaurantBeverage, RestaurantBeverageId> {
    List<RestaurantBeverage> findByRestaurantId(Long restaurantId);
    Optional<RestaurantBeverage> findByRestaurantIdAndBeverageId(Long restaurantId, Long beverageId);
}
