package club.castillo.restaurantes.castillo.repository;

import club.castillo.restaurantes.castillo.model.Beverage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BeverageRepository extends JpaRepository<Beverage, Long> {
    List<Beverage> findByActiveTrue();
    Optional<Beverage> findByIdAndActiveTrue(Long id);
    List<Beverage> findByActiveTrueAndAvailableTrue();

}
