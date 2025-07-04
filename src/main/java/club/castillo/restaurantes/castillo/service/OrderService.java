package club.castillo.restaurantes.castillo.service;

import club.castillo.restaurantes.castillo.dto.*;
import club.castillo.restaurantes.castillo.model.*;
import club.castillo.restaurantes.castillo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderBeverageRepository orderBeverageRepository;
    private final BeverageRepository beverageRepository;
    private final QrCodeRepository qrCodeRepository;
    private final OrderNotificationService notificationService;
    private final OrderWebSocketService orderWebSocketService;
    private final UserRepository userRepository;
    private final MenuItemRepository menuItemRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByRestaurant(Long restaurantId) {
        return orderRepository.findByRestaurantIdAndActiveTrue(restaurantId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long id) {
        Order order = orderRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return convertToDTO(order);
    }

    @Transactional
    public OrderDTO createOrder(String qrCode, List<OrderItemDTO> items, List<OrderBeverageDTO> beverages) {
        // DEBUG: Verificar datos recibidos
        System.out.println("=== DEBUG CREATE ORDER ===");
        System.out.println("QR Code: " + qrCode);
        System.out.println("Items recibidos: " + (items != null ? items.size() : "null"));
        System.out.println("Beverages recibidos: " + (beverages != null ? beverages.size() : "null"));

        if (beverages != null) {
            for (OrderBeverageDTO bev : beverages) {
                System.out.println("Beverage ID: " + bev.getBeverageId() + ", Quantity: " + bev.getQuantity());
            }
        }

        QrCode qrCodeEntity = qrCodeRepository.findByCodeAndActiveTrue(qrCode)
                .orElseThrow(() -> new RuntimeException("QR Code not found or inactive"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = null;
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            currentUser = userRepository.findByEmail(email).orElse(null);
        }

        Order order = Order.builder()
                .restaurant(qrCodeEntity.getRestaurant())
                .status(OrderStatus.PENDING)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .total(BigDecimal.ZERO)
                .build();

        if (currentUser != null && currentUser.getRole() != null) {
            order.setCustomer(currentUser);
            order.setCustomerName(currentUser.getFirstName() + " " + currentUser.getLastName());
            order.setGuest(false);
        } else {
            order.setCustomerName("Invitado");
            order.setGuest(true);
        }

        order = orderRepository.save(order);
        final Order finalOrder = order;

        // Calcular total
        final BigDecimal[] total = {BigDecimal.ZERO};

        // Guardar items
        if (items != null && !items.isEmpty()) {
            System.out.println("Procesando " + items.size() + " items de comida...");
            List<OrderItem> orderItems = items.stream()
                    .filter(dto -> dto.getMenuItemId() != null)
                    .map(dto -> menuItemRepository.findByIdAndActiveTrue(dto.getMenuItemId())
                            .map(menuItem -> {
                                BigDecimal subtotal = menuItem.getPrice().multiply(BigDecimal.valueOf(dto.getQuantity()));
                                total[0] = total[0].add(subtotal);
                                return OrderItem.builder()
                                        .order(finalOrder)
                                        .menuItem(menuItem)
                                        .quantity(dto.getQuantity())
                                        .unitPrice(menuItem.getPrice())
                                        .subtotal(subtotal)
                                        .build();
                            })
                            .orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            orderItemRepository.saveAll(orderItems);
            order.setItems(orderItems);
            System.out.println("Items guardados: " + orderItems.size());
        }

        // Guardar bebidas
        if (beverages != null && !beverages.isEmpty()) {
            System.out.println("Procesando " + beverages.size() + " bebidas...");
            List<OrderBeverage> orderBeverages = beverages.stream()
                    .filter(dto -> {
                        System.out.println("Filtrando beverage con ID: " + dto.getBeverageId());
                        return dto.getBeverageId() != null;
                    })
                    .map(dto -> {
                        System.out.println("Buscando beverage con ID: " + dto.getBeverageId());
                        return beverageRepository.findByIdAndActiveTrue(dto.getBeverageId())
                                .map(beverage -> {
                                    System.out.println("Beverage encontrado: " + beverage.getName() + ", Precio: " + beverage.getPrice());
                                    BigDecimal subtotal = BigDecimal.valueOf(beverage.getPrice())
                                            .multiply(BigDecimal.valueOf(dto.getQuantity()));
                                    total[0] = total[0].add(subtotal);
                                    return OrderBeverage.builder()
                                            .order(finalOrder)
                                            .beverage(beverage)
                                            .quantity(dto.getQuantity())
                                            .price(beverage.getPrice())
                                            .active(true)
                                            .createdAt(LocalDateTime.now())
                                            .build();
                                })
                                .orElseGet(() -> {
                                    System.out.println("ERROR: Beverage no encontrado con ID: " + dto.getBeverageId());
                                    return null;
                                });
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            System.out.println("OrderBeverages a guardar: " + orderBeverages.size());
            orderBeverageRepository.saveAll(orderBeverages);
            order.setBeverages(orderBeverages);
            System.out.println("Bebidas guardadas: " + orderBeverages.size());
        } else {
            System.out.println("No hay bebidas para procesar (beverages es null o vacío)");
        }

        order.setTotal(total[0]);
        order = orderRepository.save(order);
        System.out.println("Total final: " + total[0]);
        System.out.println("=== FIN DEBUG ===");

        String message = "Nueva orden recibida: " + order.getCustomerName();
        notificationService.notifyClient(order.getId(), "NEW_ORDER", message);
        orderWebSocketService.sendOrderStatusUpdate(new OrderStatusMessage(
                order.getId(),
                "NEW_ORDER",
                order.getRestaurant().getId(),
                message
        ));

        return convertToDTO(order);
    }

    @Transactional
    public OrderDTO updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findByIdAndActiveTrue(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = null;
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            currentUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        }

        if (currentUser == null ||
                (!currentUser.getRole().getName().equals("OWNER") &&
                        !(currentUser.getRole().getName().equals("RESTAURANT_ADMIN") &&
                                currentUser.getId().equals(order.getRestaurant().getAdmin().getId())))) {
            throw new AccessDeniedException("You don't have permission to update this order's status");
        }

        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
        order = orderRepository.save(order);

        String message = switch (status) {
            case CONFIRMED -> "Tu pedido ha sido confirmado.";
            case PREPARING -> "Tu pedido está siendo preparado.";
            case READY -> "Tu pedido ya está listo para retirar.";
            case CANCELLED -> "Tu pedido ha sido cancelado.";
            case DELIVERED -> "Gracias por usar el sistema.";
            default -> "Tu pedido ha sido actualizado.";
        };

        notificationService.notifyClient(orderId, status.name(), message);
        orderWebSocketService.sendOrderStatusUpdate(new OrderStatusMessage(
                order.getId(),
                status.name(),
                order.getRestaurant().getId(),
                message
        ));

        return convertToDTO(order);
    }

    @Transactional
    public void deleteOrder(Long orderId) {
        Order order = orderRepository.findByIdAndActiveTrue(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = null;
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            currentUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        }

        order.setActive(false);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    private OrderDTO convertToDTO(Order order) {
        return OrderDTO.builder()
                .id(order.getId())
                .customerId(order.getCustomer() != null ? order.getCustomer().getId() : null)
                .customerName(order.getCustomerName())
                .restaurantId(order.getRestaurant().getId())
                .items(order.getItems() != null ?
                        order.getItems().stream()
                                .map(orderItem -> OrderItemDTO.builder()
                                        .id(orderItem.getId())
                                        .menuItemId(orderItem.getMenuItem().getId())
                                        .quantity(orderItem.getQuantity())
                                        .unitPrice(orderItem.getUnitPrice())
                                        .subtotal(orderItem.getSubtotal())
                                        .build())
                                .collect(Collectors.toList()) :
                        List.of())
                .beverages(order.getBeverages() != null ?
                        order.getBeverages().stream()
                                .map(orderBeverage -> OrderBeverageDTO.builder()
                                        .id(orderBeverage.getId())
                                        .beverageId(orderBeverage.getBeverage().getId())
                                        .quantity(orderBeverage.getQuantity())
                                        .price(orderBeverage.getPrice())
                                        .build())
                                .collect(Collectors.toList()) :
                        List.of())
                .status(order.getStatus())
                .total(order.getTotal())
                .isGuest(order.isGuest())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }


    @Transactional
    public String scanOrder(Long orderId, ScanRequest scanRequest) {
        Order order = orderRepository.findByIdAndActiveTrue(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getStatus().equals(OrderStatus.READY)) {
            throw new RuntimeException("El pedido no está listo para ser entregado");
        }

        String expectedHash = generateSecureHash(orderId);
        if (!expectedHash.equals(scanRequest.getHash())) {
            throw new RuntimeException("QR inválido");
        }

        order.setStatus(OrderStatus.DELIVERED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        String message = "✅ Pedido entregado. ¡Gracias por usar el sistema!";
        notificationService.notifyClient(orderId, "DELIVERED", message);
        orderWebSocketService.sendOrderStatusUpdate(new OrderStatusMessage(
                order.getId(),
                "DELIVERED",
                order.getRestaurant().getId(),
                message
        ));

        return "Pedido entregado con éxito";
    }

    @Transactional
    public String cancelOrder(Long orderId) {
        Order order = orderRepository.findByIdAndActiveTrue(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!(order.getStatus().equals(OrderStatus.PENDING) || order.getStatus().equals(OrderStatus.CONFIRMED))) {
            throw new RuntimeException("No se puede cancelar un pedido que ya está en preparación o entregado");
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        String message = "Tu pedido ha sido cancelado correctamente.";
        notificationService.notifyClient(orderId, "CANCELLED", message);
        orderWebSocketService.sendOrderStatusUpdate(new OrderStatusMessage(
                order.getId(),
                "CANCELLED",
                order.getRestaurant().getId(),
                message
        ));

        return "Pedido cancelado";
    }

    private String generateSecureHash(Long orderId) {
        String input = orderId + "-secret";
        return org.apache.commons.codec.digest.DigestUtils.sha256Hex(input);
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = java.util.UUID.randomUUID().toString().substring(0, 8);
        } while (qrCodeRepository.existsByCodeAndActiveTrue(code));
        return code;
    }

    @Transactional
    public String generatePermanentQrCode(Long restaurantId) {
        return qrCodeRepository.findFirstByRestaurantIdAndActiveTrue(restaurantId)
                .map(QrCode::getCode)
                .orElseGet(() -> {
                    QrCode qrCode = QrCode.builder()
                            .code(generateUniqueCode())
                            .restaurant(Restaurant.builder().id(restaurantId).build())
                            .active(true)
                            .build();
                    qrCodeRepository.save(qrCode);
                    return qrCode.getCode();
                });
    }
}
