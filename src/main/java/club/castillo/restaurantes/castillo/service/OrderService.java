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

    // VALIDACIÓN CRUZADA: NO permite mezclar ítems de varios restaurantes en el mismo pedido
    @Transactional
    public OrderDTO createOrder(String qrCode, Long restaurantId, List<OrderItemDTO> items, List<OrderBeverageDTO> beverages) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = null;
        String roleName = null;
        boolean isInvited = false;
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            currentUser = userRepository.findByEmail(email).orElse(null);
            if (currentUser != null && currentUser.getRole() != null) {
                roleName = currentUser.getRole().getName();
                if ("INVITED".equalsIgnoreCase(roleName)) {
                    isInvited = true;
                }
            }
        } else {
            // Si no hay usuario autenticado, es invitado
            isInvited = true;
        }

        // Invitado requiere QR siempre
        if (isInvited) {
            if (qrCode == null || qrCode.isEmpty()) {
                throw new RuntimeException("Como invitado, es obligatorio escanear el QR del restaurante para hacer un pedido.");
            }
        }

        Restaurant restaurant = null;

        if (isInvited) {
            // 1. Buscar restaurante por QR
            QrCode qrCodeEntity = qrCodeRepository.findByCodeAndActiveTrue(qrCode)
                    .orElseThrow(() -> new RuntimeException("QR Code no encontrado o inactivo"));
            restaurant = qrCodeEntity.getRestaurant();

            // 2. Validar que TODOS los items y bebidas pertenecen al restaurante del QR
            if (items != null && !items.isEmpty()) {
                for (OrderItemDTO dto : items) {
                    if (dto.getMenuItemId() != null) {
                        MenuItem menuItem = menuItemRepository.findByIdAndActiveTrue(dto.getMenuItemId())
                                .orElseThrow(() -> new RuntimeException("Ítem del menú no existe o está inactivo"));
                        if (!menuItem.getRestaurant().getId().equals(restaurant.getId())) {
                            throw new RuntimeException("El ítem '" + menuItem.getName() + "' no pertenece al restaurante del QR escaneado.");
                        }
                    }
                }
            }
            // Bebidas (si tienes la lógica de bebidas por restaurante, valida aquí también)
            // ... puedes incluir lógica similar si es necesario

        } else {
            // Usuario registrado: debe venir restaurantId, nunca QR
            if (restaurantId == null) {
                throw new RuntimeException("Debes enviar el ID del restaurante para realizar el pedido.");
            }
            restaurant = Restaurant.builder().id(restaurantId).build();
        }

        String customerName = (currentUser != null && currentUser.getFirstName() != null)
                ? currentUser.getFirstName() + " " + (currentUser.getLastName() != null ? currentUser.getLastName() : "")
                : "Invitado";

        Order order = Order.builder()
                .restaurant(restaurant)
                .status(OrderStatus.PENDING)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .total(BigDecimal.ZERO)
                .customer(currentUser)
                .customerName(customerName)
                .isGuest(isInvited)
                .build();

        order = orderRepository.save(order);
        final Order finalOrder = order;

        // Calcular total
        final BigDecimal[] total = {BigDecimal.ZERO};

        // Guardar items
        if (items != null && !items.isEmpty()) {
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
        }

        // Guardar bebidas
        if (beverages != null && !beverages.isEmpty()) {
            List<OrderBeverage> orderBeverages = beverages.stream()
                    .filter(dto -> dto.getBeverageId() != null)
                    .map(dto -> beverageRepository.findByIdAndActiveTrue(dto.getBeverageId())
                            .map(beverage -> {
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
                            .orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            orderBeverageRepository.saveAll(orderBeverages);
            order.setBeverages(orderBeverages);
        }

        order.setTotal(total[0]);
        order = orderRepository.save(order);

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
                                        .name(orderItem.getMenuItem().getName())
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
                                        .name(orderBeverage.getBeverage().getName())
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
