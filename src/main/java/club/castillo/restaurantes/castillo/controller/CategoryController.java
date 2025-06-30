package club.castillo.restaurantes.castillo.controller;

import club.castillo.restaurantes.castillo.dto.CategoryDTO;
import club.castillo.restaurantes.castillo.model.CategoryType;
import club.castillo.restaurantes.castillo.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    // ------ BEBIDAS ------
    @GetMapping("/beverages")
    public ResponseEntity<List<CategoryDTO>> getBeverageCategories() {
        return ResponseEntity.ok(categoryService.getAllCategoriesByType(CategoryType.BEVERAGE));
    }

    // SOLO OWNER puede crear categorías de bebidas (centralizado)
    @PreAuthorize("hasRole('OWNER')")
    @PostMapping("/beverages")
    public ResponseEntity<CategoryDTO> createBeverageCategory(@RequestBody CategoryDTO categoryDTO) {
        categoryDTO.setType("BEVERAGE");
        return ResponseEntity.ok(categoryService.createCategory(categoryDTO));
    }

    // ------ COMIDA ------
    @GetMapping("/food")
    public ResponseEntity<List<CategoryDTO>> getFoodCategories() {
        return ResponseEntity.ok(categoryService.getAllCategoriesByType(CategoryType.FOOD));
    }

    // RESTAURANT_ADMIN u OWNER pueden crear categorías de comida
    @PreAuthorize("hasRole('RESTAURANT_ADMIN') or hasRole('OWNER')")
    @PostMapping("/food")
    public ResponseEntity<CategoryDTO> createFoodCategory(@RequestBody CategoryDTO categoryDTO) {
        categoryDTO.setType("FOOD");
        return ResponseEntity.ok(categoryService.createCategory(categoryDTO));
    }
}
