package club.castillo.restaurantes.castillo.repository;

import club.castillo.restaurantes.castillo.model.Category;
import club.castillo.restaurantes.castillo.model.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByTypeAndActiveTrue(CategoryType type);
}
