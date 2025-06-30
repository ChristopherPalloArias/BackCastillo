package club.castillo.restaurantes.castillo.repository;

import club.castillo.restaurantes.castillo.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    List<Restaurant> findByActiveTrue();
    Optional<Restaurant> findByIdAndActiveTrue(Long id);
    List<Restaurant> findByZoneId(Long zoneId);
    List<Restaurant> findByAdminId(Long adminId);
    Optional<Restaurant> findByAdminIdAndActiveTrue(Long adminId);

} 