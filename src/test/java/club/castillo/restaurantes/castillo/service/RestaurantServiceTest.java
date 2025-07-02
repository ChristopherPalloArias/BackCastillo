package club.castillo.restaurantes.castillo.service;

import club.castillo.restaurantes.castillo.dto.RestaurantDTO;
import club.castillo.restaurantes.castillo.model.*;
import club.castillo.restaurantes.castillo.repository.RestaurantRepository;
import club.castillo.restaurantes.castillo.repository.UserRepository;
import club.castillo.restaurantes.castillo.repository.ZoneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para RestaurantService.
 *
 * Casos cubiertos:
 *  1. Creación de restaurante con admin válido y zona existente.
 *  2. Actualización de restaurante por un RESTAURANT_ADMIN autorizado.
 *  3. Intento de actualización por un RESTAURANT_ADMIN sobre restaurante ajeno (denegado).
 *  4. Eliminación (soft delete) por OWNER y por RESTAURANT_ADMIN propio.
 *  5. Intento de eliminación por usuario sin permisos (denegado).
 */
class RestaurantServiceTest {

    private RestaurantRepository restaurantRepository;
    private UserRepository userRepository;
    private ZoneRepository zoneRepository;
    private RestaurantService restaurantService;

    @BeforeEach
    void setUp() {
        restaurantRepository = mock(RestaurantRepository.class);
        userRepository = mock(UserRepository.class);
        zoneRepository = mock(ZoneRepository.class);
        restaurantService = new RestaurantService(restaurantRepository, userRepository, zoneRepository);
    }

    /**
     * Prueba la creación de restaurante con admin válido y zona existente.
     */
    @Test
    void testCreateRestaurant_Success() {
        // Arrange
        User admin = new User();
        admin.setId(5L);
        Role adminRole = new Role();
        adminRole.setName(RoleType.RESTAURANT_ADMIN);
        admin.setRole(adminRole);

        Zone zone = Zone.builder().id(1L).name("Zona 1").build();

        Restaurant saved = Restaurant.builder()
                .id(10L)
                .name("La Parrilla")
                .description("Especialidad en carnes")
                .admin(admin)
                .zone(zone)
                .status(RestaurantStatus.CLOSED)
                .active(true)
                .build();

        RestaurantDTO dto = RestaurantDTO.builder()
                .name("La Parrilla")
                .description("Especialidad en carnes")
                .adminId(5L)
                .zoneId(1L)
                .build();

        when(userRepository.findById(5L)).thenReturn(Optional.of(admin));
        when(zoneRepository.findById(1L)).thenReturn(Optional.of(zone));
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(saved);

        // Act
        RestaurantDTO result = restaurantService.createRestaurant(dto);

        // Assert
        assertNotNull(result);
        assertEquals("La Parrilla", result.getName());
        assertEquals("Especialidad en carnes", result.getDescription());
        assertEquals(5L, result.getAdminId());
        assertEquals(1L, result.getZoneId());
        verify(restaurantRepository, times(1)).save(any(Restaurant.class));
    }

    /**
     * Prueba la actualización de restaurante por su propio RESTAURANT_ADMIN.
     */
    @Test
    void testUpdateRestaurant_SuccessByAdmin() {
        // Arrange
        User admin = new User();
        admin.setId(7L);
        Role adminRole = new Role();
        adminRole.setName(RoleType.RESTAURANT_ADMIN);
        admin.setRole(adminRole);

        Zone zone = Zone.builder().id(2L).name("Zona B").build();
        Restaurant restaurant = Restaurant.builder()
                .id(20L)
                .name("Pizza Club")
                .description("Pizzería tradicional")
                .admin(admin)
                .zone(zone)
                .status(RestaurantStatus.CLOSED)
                .active(true)
                .build();

        RestaurantDTO dto = RestaurantDTO.builder()
                .name("Pizza Club Renovado")
                .description("Ahora con pastas")
                .adminId(7L)
                .zoneId(2L)
                .status("OPEN")
                .build();

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(admin);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(restaurantRepository.findByIdAndActiveTrue(20L)).thenReturn(Optional.of(restaurant));
        when(userRepository.findById(7L)).thenReturn(Optional.of(admin));
        when(zoneRepository.findById(2L)).thenReturn(Optional.of(zone));
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(restaurant);

        // Act
        RestaurantDTO result = restaurantService.updateRestaurant(20L, dto);

        // Assert
        assertNotNull(result);
        assertEquals("Pizza Club Renovado", result.getName());
        assertEquals("Ahora con pastas", result.getDescription());
        assertEquals("OPEN", result.getStatus());
        verify(restaurantRepository, times(1)).save(any(Restaurant.class));
    }

    /**
     * Prueba que un RESTAURANT_ADMIN no puede actualizar restaurantes ajenos.
     */
    @Test
    void testUpdateRestaurant_DeniedForOtherAdmin() {
        // Arrange
        User admin1 = new User();
        admin1.setId(11L);
        Role adminRole = new Role();
        adminRole.setName(RoleType.RESTAURANT_ADMIN);
        admin1.setRole(adminRole);

        User admin2 = new User();
        admin2.setId(12L);
        admin2.setRole(adminRole);

        Zone zone = Zone.builder().id(3L).name("Zona X").build();
        Restaurant restaurant = Restaurant.builder()
                .id(30L)
                .name("El Ajeno")
                .description("No modificable por otros")
                .admin(admin2)
                .zone(zone)
                .status(RestaurantStatus.CLOSED)
                .active(true)
                .build();

        RestaurantDTO dto = RestaurantDTO.builder()
                .name("Cambio Ilegal")
                .description("Intento de acceso no autorizado")
                .adminId(11L)
                .zoneId(3L)
                .status("OPEN")
                .build();

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(admin1);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(restaurantRepository.findByIdAndActiveTrue(30L)).thenReturn(Optional.of(restaurant));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> restaurantService.updateRestaurant(30L, dto));
    }

    /**
     * Prueba eliminación (soft delete) por OWNER.
     */
    @Test
    void testDeleteRestaurant_SuccessAsOwner() {
        // Arrange
        User owner = new User();
        owner.setId(100L);
        Role ownerRole = new Role();
        ownerRole.setName(RoleType.OWNER);
        owner.setRole(ownerRole);

        User admin = new User();
        admin.setId(55L);
        Role adminRole = new Role();
        adminRole.setName(RoleType.RESTAURANT_ADMIN);
        admin.setRole(adminRole);

        Zone zone = Zone.builder().id(8L).name("Zona Elite").build();
        Restaurant restaurant = Restaurant.builder()
                .id(99L)
                .name("El Club")
                .description("VIP")
                .admin(admin)
                .zone(zone)
                .status(RestaurantStatus.OPEN)
                .active(true)
                .build();

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(owner);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(restaurantRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.of(restaurant));
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(restaurant);

        // Act
        restaurantService.deleteRestaurant(99L);

        // Assert
        assertFalse(restaurant.isActive());
        verify(restaurantRepository, times(1)).save(any(Restaurant.class));
    }

    /**
     * Prueba eliminación (soft delete) por RESTAURANT_ADMIN de su propio restaurante.
     */
    @Test
    void testDeleteRestaurant_SuccessByOwnAdmin() {
        // Arrange
        User admin = new User();
        admin.setId(21L);
        Role adminRole = new Role();
        adminRole.setName(RoleType.RESTAURANT_ADMIN);
        admin.setRole(adminRole);

        Zone zone = Zone.builder().id(5L).name("Zona Oeste").build();
        Restaurant restaurant = Restaurant.builder()
                .id(77L)
                .name("Mi Restaurante")
                .description("Mi lugar")
                .admin(admin)
                .zone(zone)
                .status(RestaurantStatus.CLOSED)
                .active(true)
                .build();

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(admin);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(restaurantRepository.findByIdAndActiveTrue(77L)).thenReturn(Optional.of(restaurant));
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(restaurant);

        // Act
        restaurantService.deleteRestaurant(77L);

        // Assert
        assertFalse(restaurant.isActive());
        verify(restaurantRepository, times(1)).save(any(Restaurant.class));
    }

    /**
     * Prueba que un usuario sin permisos no puede eliminar el restaurante.
     */
    @Test
    void testDeleteRestaurant_DeniedForUnauthorizedUser() {
        // Arrange
        User customer = new User();
        customer.setId(88L);
        Role customerRole = new Role();
        customerRole.setName(RoleType.CUSTOMER);
        customer.setRole(customerRole);

        User admin = new User();
        admin.setId(22L);
        Role adminRole = new Role();
        adminRole.setName(RoleType.RESTAURANT_ADMIN);
        admin.setRole(adminRole);

        Zone zone = Zone.builder().id(6L).name("Zona C").build();
        Restaurant restaurant = Restaurant.builder()
                .id(44L)
                .name("Sin Permisos")
                .description("Intento indebido")
                .admin(admin)
                .zone(zone)
                .status(RestaurantStatus.CLOSED)
                .active(true)
                .build();

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(customer);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(restaurantRepository.findByIdAndActiveTrue(44L)).thenReturn(Optional.of(restaurant));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> restaurantService.deleteRestaurant(44L));
    }
}
