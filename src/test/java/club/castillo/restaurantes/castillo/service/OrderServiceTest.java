package club.castillo.restaurantes.castillo.service;

import club.castillo.restaurantes.castillo.dto.OrderBeverageDTO;
import club.castillo.restaurantes.castillo.dto.OrderDTO;
import club.castillo.restaurantes.castillo.model.*;
import club.castillo.restaurantes.castillo.repository.BeverageRepository;
import club.castillo.restaurantes.castillo.repository.OrderBeverageRepository;
import club.castillo.restaurantes.castillo.repository.OrderRepository;
import club.castillo.restaurantes.castillo.repository.QrCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para OrderService.
 *
 * Casos cubiertos:
 *  1. Creación de orden a partir de un QR válido y lista de bebidas.
 *  2. Actualización de estado de una orden por OWNER (rol autorizado).
 *  3. Intento de actualizar el estado por usuario no autorizado (permiso denegado).
 *
 * Se usan mocks para simular dependencias y permisos.
 */
class OrderServiceTest {

    private OrderRepository orderRepository;
    private OrderBeverageRepository orderBeverageRepository;
    private BeverageRepository beverageRepository;
    private QrCodeRepository qrCodeRepository;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        orderBeverageRepository = mock(OrderBeverageRepository.class);
        beverageRepository = mock(BeverageRepository.class);
        qrCodeRepository = mock(QrCodeRepository.class);
        orderService = new OrderService(orderRepository, orderBeverageRepository, beverageRepository, qrCodeRepository);
    }

    /**
     * Prueba la creación exitosa de una orden a partir de un QR válido y bebidas.
     */
    @Test
    void testCreateOrder_Success() {
        // Arrange
        Restaurant restaurant = Restaurant.builder().id(1L).name("Castillo").build();
        QrCode qr = QrCode.builder().code("QR123").active(true).restaurant(restaurant).tableNumber("2").build();

        Beverage beverage = Beverage.builder()
                .id(10L)
                .name("Cola")
                .price(2.5) // <-- Usa Double directamente
                .active(true)
                .build();

        OrderBeverageDTO orderBeverageDTO = OrderBeverageDTO.builder()
                .beverageId(10L)
                .quantity(2)
                .price(2.5) // DTO: Double
                .build();

        when(qrCodeRepository.findByCodeAndActiveTrue("QR123")).thenReturn(Optional.of(qr));
        when(beverageRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(beverage));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(100L);
            o.setBeverages(Collections.emptyList());
            return o;
        });

        // Act
        OrderDTO result = orderService.createOrder("QR123", Collections.singletonList(orderBeverageDTO));

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(1L, result.getRestaurantId());
        assertEquals("2", result.getTableNumber());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderBeverageRepository, times(1)).saveAll(any());
    }

    /**
     * Prueba la actualización de estado de una orden por un usuario OWNER (autorizado).
     */
    @Test
    void testUpdateOrderStatus_SuccessAsOwner() {
        // Arrange
        Role ownerRole = new Role();
        ownerRole.setName("OWNER");
        User owner = new User();
        owner.setId(1L);
        owner.setRole(ownerRole);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(owner);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Restaurant restaurant = Restaurant.builder().id(2L).admin(owner).build();
        Order order = Order.builder().id(200L).restaurant(restaurant).active(true).status(OrderStatus.PENDING).build();
        order.setBeverages(Collections.emptyList());

        when(orderRepository.findByIdAndActiveTrue(200L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrderDTO result = orderService.updateOrderStatus(200L, OrderStatus.READY);

        // Assert
        assertNotNull(result);
        assertEquals(OrderStatus.READY, result.getStatus());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    /**
     * Prueba que un usuario no autorizado no puede actualizar el estado de una orden.
     */
    @Test
    void testUpdateOrderStatus_DeniedForCustomer() {
        // Arrange
        Role customerRole = new Role();
        customerRole.setName("CUSTOMER");
        User customer = new User();
        customer.setId(3L);
        customer.setRole(customerRole);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(customer);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Role ownerRole = new Role();
        ownerRole.setName("OWNER");
        User owner = new User();
        owner.setId(1L);
        owner.setRole(ownerRole);

        Restaurant restaurant = Restaurant.builder().id(2L).admin(owner).build();
        Order order = Order.builder().id(300L).restaurant(restaurant).active(true).status(OrderStatus.PENDING).build();
        order.setBeverages(Collections.emptyList());

        when(orderRepository.findByIdAndActiveTrue(300L)).thenReturn(Optional.of(order));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> orderService.updateOrderStatus(300L, OrderStatus.READY));
    }
}
