package club.castillo.restaurantes.castillo.service;

import club.castillo.restaurantes.castillo.dto.CategoryDTO;
import club.castillo.restaurantes.castillo.model.Category;
import club.castillo.restaurantes.castillo.model.CategoryType;
import club.castillo.restaurantes.castillo.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategoriesByType(CategoryType type) {
        return categoryRepository.findByTypeAndActiveTrue(type)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryDTO createCategory(CategoryDTO dto) {
        Category category = Category.builder()
                .name(dto.getName())
                .type(CategoryType.valueOf(dto.getType()))
                .active(true)
                .build();
        return toDTO(categoryRepository.save(category));
    }

    private CategoryDTO toDTO(Category c) {
        return CategoryDTO.builder()
                .id(c.getId())
                .name(c.getName())
                .type(c.getType().name())
                .build();
    }
}
