package club.castillo.restaurantes.castillo.service;

import club.castillo.restaurantes.castillo.dto.OrderBeverageDTO;
import club.castillo.restaurantes.castillo.dto.OrderDTO;
import club.castillo.restaurantes.castillo.dto.ScanRequest;
import club.castillo.restaurantes.castillo.model.*;
import club.castillo.restaurantes.castillo.repository.BeverageRepository;
import club.castillo.restaurantes.castillo.repository.OrderBeverageRepository;
import club.castillo.restaurantes.castillo.repository.OrderRepository;
import club.castillo.restaurantes.castillo.repository.QrCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderBeverageRepository orderBeverageRepository;
    private final BeverageRepository beverageRepository;
    private final QrCodeRepository qrCodeRepository;
    private final OrderNotificationService notificationService;

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
    public OrderDTO createOrder(String qrCode, List<OrderBeverageDTO> beverages) {
        QrCode qrCodeEntity = qrCodeRepository.findByCodeAndActiveTrue(qrCode)
                .orElseThrow(() -> new RuntimeException("QR Code not found or inactive"));

        Order order = Order.builder()
                .restaurant(qrCodeEntity.getRestaurant())
                .tableNumber(qrCodeEntity.getTableNumber())
                .status(OrderStatus.PENDING)
                .active(true)
                .build();

        order = orderRepository.save(order);

        // SOLUCIÓN: Hacer order final para usar en la lambda
        final Order finalOrder = order;

        List<OrderBeverage> orderBeverages = beverages.stream()
                .map(beverageDTO -> {
                    Beverage beverage = beverageRepository.findByIdAndActiveTrue(beverageDTO.getBeverageId())
                            .orElseThrow(() -> new RuntimeException("Beverage not found"));

                    return OrderBeverage.builder()
                            .order(finalOrder) // <-- aquí el cambio
                            .beverage(beverage)
                            .quantity(beverageDTO.getQuantity())
                            .price(beverage.getPrice())
                            .active(true)
                            .build();
                })
                .collect(Collectors.toList());

        orderBeverageRepository.saveAll(orderBeverages);
        order.setBeverages(orderBeverages);

        return convertToDTO(order);
    }

    @Transactional
    public OrderDTO updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findByIdAndActiveTrue(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        if (!currentUser.getRole().getName().equals("OWNER") &&
                !(currentUser.getRole().getName().equals("RESTAURANT_ADMIN") &&
                        currentUser.getId().equals(order.getRestaurant().getAdmin().getId()))) {
            throw new AccessDeniedException("You don't have permission to update this order's status");
        }

        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
        order = orderRepository.save(order);

        // 🔔 Notificar cliente según estado
        String message = switch (status) {
            case CONFIRMED -> "Tu pedido ha sido confirmado.";
            case PREPARING -> "Tu pedido está siendo preparado.";
            case READY -> "Tu pedido ya está listo para retirar.";
            case CANCELLED -> "Tu pedido ha sido cancelado.";
            case DELIVERED -> "Gracias por usar el sistema.";
            default -> "Tu pedido ha sido actualizado.";
        };

        notificationService.notifyClient(orderId, status.name(), message);

        return convertToDTO(order);
    }

    @Transactional
    public void deleteOrder(Long orderId) {
        Order order = orderRepository.findByIdAndActiveTrue(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Verificar permisos
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        if (!currentUser.getRole().getName().equals("OWNER") &&
            !(currentUser.getRole().getName().equals("RESTAURANT_ADMIN") &&
              currentUser.getId().equals(order.getRestaurant().getAdmin().getId()))) {
            throw new AccessDeniedException("You don't have permission to delete this order");
        }

        order.setActive(false);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    private OrderDTO convertToDTO(Order order) {
        return OrderDTO.builder()
                .id(order.getId())
                .restaurantId(order.getRestaurant().getId())
                .tableNumber(order.getTableNumber())
                .status(order.getStatus())
                .beverages(order.getBeverages().stream()
                        .map(orderBeverage -> OrderBeverageDTO.builder()
                                .id(orderBeverage.getId())
                                .beverageId(orderBeverage.getBeverage().getId())
                                .quantity(orderBeverage.getQuantity())
                                .price(orderBeverage.getPrice())
                                .build())
                        .collect(Collectors.toList()))
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

        notificationService.notifyClient(orderId, "DELIVERED", "✅ Pedido entregado. ¡Gracias por usar el sistema!");

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

        notificationService.notifyClient(orderId, "CANCELLED", "Tu pedido ha sido cancelado correctamente.");

        return "Pedido cancelado";
    }

    private String generateSecureHash(Long orderId) {
        String input = orderId + "-secret";
        return org.apache.commons.codec.digest.DigestUtils.sha256Hex(input);
    }

}
