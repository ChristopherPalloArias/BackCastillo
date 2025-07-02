package club.castillo.restaurantes.castillo.service;

import club.castillo.restaurantes.castillo.dto.CategoryDTO;
import club.castillo.restaurantes.castillo.model.Category;
import club.castillo.restaurantes.castillo.model.CategoryType;
import club.castillo.restaurantes.castillo.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para el servicio CategoryService.
 *
 * Estos tests validan los dos escenarios principales del servicio:
 *  1. Obtener todas las categorías activas de un tipo específico (getAllCategoriesByType).
 *  2. Crear una nueva categoría a partir de un DTO (createCategory).
 *
 * Se utilizan mocks para el repositorio, asegurando que los métodos funcionen de forma aislada
 * y correcta, transformando los datos según lo esperado.
 */
class CategoryServiceTest {

    private CategoryRepository categoryRepository;
    private CategoryService categoryService;

    /**
     * Inicializa los mocks y la instancia de servicio antes de cada test.
     */
    @BeforeEach
    void setUp() {
        categoryRepository = mock(CategoryRepository.class);
        categoryService = new CategoryService(categoryRepository);
    }

    /**
     * Prueba el método getAllCategoriesByType, asegurando que:
     * - Se consulta correctamente al repositorio según el tipo.
     * - Los datos retornados se transforman correctamente a DTOs.
     */
    @Test
    void testGetAllCategoriesByType() {
        // Arrange: Prepara dos categorías simuladas del tipo BEVERAGE
        Category c1 = Category.builder().id(1L).name("Refrescos").type(CategoryType.BEVERAGE).active(true).build();
        Category c2 = Category.builder().id(2L).name("Jugos").type(CategoryType.BEVERAGE).active(true).build();

        when(categoryRepository.findByTypeAndActiveTrue(CategoryType.BEVERAGE))
                .thenReturn(Arrays.asList(c1, c2));

        // Act: Llama al método del servicio
        List<CategoryDTO> result = categoryService.getAllCategoriesByType(CategoryType.BEVERAGE);

        // Assert: Verifica el tamaño y los valores retornados
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Refrescos", result.get(0).getName());
        assertEquals("Jugos", result.get(1).getName());
        assertEquals("BEVERAGE", result.get(0).getType());
        verify(categoryRepository, times(1)).findByTypeAndActiveTrue(CategoryType.BEVERAGE);
    }

    /**
     * Prueba el método createCategory, asegurando que:
     * - Se construye correctamente la entidad a partir del DTO.
     * - Se guarda la categoría y se retorna el DTO esperado.
     */
    @Test
    void testCreateCategory() {
        // Arrange: Prepara el DTO de entrada y la categoría simulada como guardada
        CategoryDTO dto = CategoryDTO.builder()
                .name("Carnes")
                .type("FOOD")
                .build();

        Category saved = Category.builder()
                .id(10L)
                .name("Carnes")
                .type(CategoryType.FOOD)
                .active(true)
                .build();

        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        // Act: Llama al método del servicio
        CategoryDTO result = categoryService.createCategory(dto);

        // Assert: Verifica que el resultado es correcto
        assertNotNull(result);
        assertEquals("Carnes", result.getName());
        assertEquals("FOOD", result.getType());
        assertEquals(10L, result.getId());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }
}
