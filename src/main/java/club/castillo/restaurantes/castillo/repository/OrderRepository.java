package club.castillo.restaurantes.castillo.repository;

import club.castillo.restaurantes.castillo.model.Order;
import club.castillo.restaurantes.castillo.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerId(Long customerId);
    List<Order> findByRestaurantId(Long restaurantId);
    List<Order> findByRestaurantIdAndStatus(Long restaurantId, OrderStatus status);
    List<Order> findByRestaurantIdAndCreatedAtBetween(Long restaurantId, LocalDateTime start, LocalDateTime end);
    List<Order> findByRestaurantIdAndActiveTrue(Long restaurantId);
    Optional<Order> findByIdAndActiveTrue(Long id);
} 