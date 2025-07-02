package club.castillo.restaurantes.castillo.service;

import club.castillo.restaurantes.castillo.dto.MenuItemDTO;
import club.castillo.restaurantes.castillo.model.*;
import club.castillo.restaurantes.castillo.repository.CategoryRepository;
import club.castillo.restaurantes.castillo.repository.MenuItemRepository;
import club.castillo.restaurantes.castillo.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para el servicio MenuItemService.
 *
 * Este set de pruebas valida los escenarios principales:
 *  1. Que un usuario con rol RESTAURANT_ADMIN u OWNER puede crear un plato correctamente,
 *     y que los datos retornados se ajustan a lo esperado.
 *  2. Que un usuario sin esos roles (por ejemplo, CUSTOMER) no puede crear platos,
 *     y el sistema lanza una excepción de seguridad.
 *
 * Mockito es utilizado para simular los repositorios y el contexto de seguridad.
 */
class MenuItemServiceTest {

    private MenuItemRepository menuItemRepository;
    private CategoryRepository categoryRepository;
    private RestaurantRepository restaurantRepository;
    private MenuItemService menuItemService;

    /**
     * Inicializa los mocks y la instancia de servicio antes de cada test.
     * También simula un usuario autenticado con rol RESTAURANT_ADMIN.
     */
    @BeforeEach
    void setUp() {
        menuItemRepository = mock(MenuItemRepository.class);
        categoryRepository = mock(CategoryRepository.class);
        restaurantRepository = mock(RestaurantRepository.class);
        menuItemService = new MenuItemService(menuItemRepository, categoryRepository, restaurantRepository);

        // Prepara un usuario con rol RESTAURANT_ADMIN
        User mockUser = new User();
        Role adminRole = new Role();
        adminRole.setName("RESTAURANT_ADMIN");
        mockUser.setRole(adminRole);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(mockUser);

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * Prueba la creación exitosa de un plato por un usuario autorizado.
     * Verifica que los datos sean correctos y se llame al repositorio.
     */
    @Test
    void testCreateMenuItem_Success() {
        // Arrange
        MenuItemDTO dto = MenuItemDTO.builder()
                .name("Lomo saltado")
                .description("Plato peruano")
                .price(BigDecimal.valueOf(10.0))
                .categoryId(1L)
                .restaurantId(5L)
                .active(true)
                .available(true)
                .build();

        Category mockCategory = Category.builder().id(1L).name("Carnes").build();
        Restaurant mockRestaurant = Restaurant.builder().id(5L).name("El Castillo").build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(mockCategory));
        when(restaurantRepository.findById(5L)).thenReturn(Optional.of(mockRestaurant));
        when(menuItemRepository.save(any(MenuItem.class))).thenAnswer(invocation -> {
            MenuItem item = invocation.getArgument(0);
            item.setId(99L);
            return item;
        });

        // Act
        MenuItemDTO result = menuItemService.createMenuItem(dto);

        // Assert
        assertNotNull(result);
        assertEquals("Lomo saltado", result.getName());
        assertEquals(BigDecimal.valueOf(10.0), result.getPrice());
        assertEquals(1L, result.getCategoryId());
        assertEquals("Carnes", result.getCategoryName());
        assertEquals(5L, result.getRestaurantId());
        assertEquals("El Castillo", result.getRestaurantName());
        assertEquals(99L, result.getId());
        verify(menuItemRepository, times(1)).save(any(MenuItem.class));
    }

    /**
     * Prueba que un usuario sin el rol RESTAURANT_ADMIN u OWNER no puede crear platos.
     * Debe lanzar una AccessDeniedException.
     */
    @Test
    void testCreateMenuItem_AccessDeniedForCustomer() {
        // Cambia el usuario autenticado a CUSTOMER
        User customerUser = new User();
        Role customerRole = new Role();
        customerRole.setName("CUSTOMER");
        customerUser.setRole(customerRole);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(customerUser);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        MenuItemDTO dto = MenuItemDTO.builder()
                .name("Hamburguesa")
                .price(BigDecimal.valueOf(8.5))
                .categoryId(1L)
                .restaurantId(5L)
                .active(true)
                .available(true)
                .build();

        assertThrows(AccessDeniedException.class, () -> menuItemService.createMenuItem(dto));
    }
}
