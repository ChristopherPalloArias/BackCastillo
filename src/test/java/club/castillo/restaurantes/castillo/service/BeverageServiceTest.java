package club.castillo.restaurantes.castillo.service;

import club.castillo.restaurantes.castillo.dto.BeverageDTO;
import club.castillo.restaurantes.castillo.model.*;
import club.castillo.restaurantes.castillo.repository.BeverageRepository;
import club.castillo.restaurantes.castillo.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para el servicio BeverageService.
 *
 * Este conjunto de tests valida dos escenarios clave:
 *  1. Que un usuario con rol OWNER puede crear correctamente una bebida,
 *     verificando que los datos se guardan correctamente y el resultado es válido.
 *  2. Que un usuario sin rol OWNER (por ejemplo, CUSTOMER) no puede crear bebidas,
 *     y el sistema arroja la excepción de seguridad correspondiente.
 *
 * Estas pruebas usan Mockito para simular las dependencias y el contexto de seguridad.
 */
class BeverageServiceTest {

    private BeverageRepository beverageRepository;
    private CategoryRepository categoryRepository;
    private BeverageService beverageService;

    /**
     * Antes de cada test se inicializan los mocks y se configura el contexto de seguridad
     * para simular un usuario autenticado con rol OWNER.
     */
    @BeforeEach
    void setUp() {
        beverageRepository = mock(BeverageRepository.class);
        categoryRepository = mock(CategoryRepository.class);
        beverageService = new BeverageService(beverageRepository, categoryRepository);

        // Simula que el usuario autenticado es un OWNER
        User mockUser = new User();
        Role ownerRole = new Role();
        ownerRole.setName(RoleType.OWNER);
        mockUser.setRole(ownerRole);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(mockUser);

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * Prueba que un usuario OWNER puede crear correctamente una bebida.
     * El test verifica que:
     * - Se asignan correctamente los datos a la bebida.
     * - El repositorio guarda la bebida y devuelve el resultado esperado.
     */
    @Test
    void testCreateBeverage_Success() {
        // Arrange: Prepara el DTO de entrada y la categoría simulada
        BeverageDTO dto = BeverageDTO.builder()
                .name("Coca-Cola")
                .description("Refresco clásico")
                .price(2.5)
                .categoryId(1L)
                .available(true)
                .build();

        Category mockCategory = new Category();
        mockCategory.setId(1L);
        mockCategory.setName("Refrescos");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(mockCategory));
        when(beverageRepository.save(any(Beverage.class))).thenAnswer(invocation -> {
            Beverage b = invocation.getArgument(0);
            b.setId(100L); // Simula que la bebida fue guardada con ID 100
            return b;
        });

        // Act: Llama al servicio para crear la bebida
        BeverageDTO result = beverageService.createBeverage(dto);

        // Assert: Verifica que la bebida fue creada correctamente
        assertNotNull(result);
        assertEquals("Coca-Cola", result.getName());
        assertEquals(2.5, result.getPrice());
        assertEquals(1L, result.getCategoryId());
        assertEquals("Refrescos", result.getCategoryName());
        assertEquals(100L, result.getId());
        verify(beverageRepository, times(1)).save(any(Beverage.class));
    }

    /**
     * Prueba que un usuario sin el rol OWNER no puede crear bebidas.
     * El test verifica que se lanza una AccessDeniedException, reforzando la seguridad del sistema.
     */
    @Test
    void testCreateBeverage_ThrowsIfNotOwner() {
        // Arrange: Simula un usuario con rol CUSTOMER (no OWNER)
        User notOwner = new User();
        Role customerRole = new Role();
        customerRole.setName(RoleType.CUSTOMER);
        notOwner.setRole(customerRole);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(notOwner);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        BeverageDTO dto = BeverageDTO.builder()
                .name("Sprite")
                .price(1.5)
                .categoryId(1L)
                .available(true)
                .build();

        // Assert: Se espera que la operación lance una excepción de acceso denegado
        assertThrows(AccessDeniedException.class, () -> beverageService.createBeverage(dto));
    }
}
