package club.castillo.restaurantes.castillo.repository;


import club.castillo.restaurantes.castillo.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
