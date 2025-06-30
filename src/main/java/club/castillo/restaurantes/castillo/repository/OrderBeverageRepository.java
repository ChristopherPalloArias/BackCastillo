package club.castillo.restaurantes.castillo.repository;

import club.castillo.restaurantes.castillo.model.OrderBeverage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderBeverageRepository extends JpaRepository<OrderBeverage, Long> {
} 