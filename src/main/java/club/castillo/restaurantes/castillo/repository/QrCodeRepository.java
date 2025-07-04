package club.castillo.restaurantes.castillo.repository;

import club.castillo.restaurantes.castillo.model.QrCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QrCodeRepository extends JpaRepository<QrCode, Long> {
    List<QrCode> findByActiveTrue();
    Optional<QrCode> findByIdAndActiveTrue(Long id);
    List<QrCode> findByRestaurantIdAndActiveTrue(Long restaurantId);
    Optional<QrCode> findByCodeAndActiveTrue(String code);
    boolean existsByCodeAndActiveTrue(String code);
    Optional<QrCode> findFirstByRestaurantIdAndActiveTrue(Long restaurantId);
} 