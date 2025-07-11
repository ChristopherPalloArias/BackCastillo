package club.castillo.restaurantes.castillo.controller;

import club.castillo.restaurantes.castillo.dto.*;
import club.castillo.restaurantes.castillo.model.OrderStatus;
import club.castillo.restaurantes.castillo.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<OrderDTO>> getOrdersByRestaurant(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(orderService.getOrdersByRestaurant(restaurantId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    // Cambiado: qrCode es opcional (required = false)
    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(
            @RequestParam(required = false) String qrCode,
            @RequestParam(required = false) Long restaurantId,
            @RequestBody OrderRequestDTO request) {
        return ResponseEntity.ok(orderService.createOrder(qrCode, restaurantId, request.getItems(), request.getBeverages()));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderDTO> updateOrderStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/scan")
    public ResponseEntity<String> scanOrderQr(@PathVariable Long id, @RequestBody ScanRequest request) {
        return ResponseEntity.ok(orderService.scanOrder(id, request));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<String> cancelOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancelOrder(id));
    }

    @PostMapping("/generate-permanent-qr")
    public ResponseEntity<String> generatePermanentQr(@RequestParam Long restaurantId) {
        String code = orderService.generatePermanentQrCode(restaurantId);
        return ResponseEntity.ok(code);
    }
}
