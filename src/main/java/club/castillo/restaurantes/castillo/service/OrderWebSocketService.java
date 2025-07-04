package club.castillo.restaurantes.castillo.service;

import club.castillo.restaurantes.castillo.dto.OrderStatusMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderWebSocketService {
    private final SimpMessagingTemplate messagingTemplate;

    public void sendOrderStatusUpdate(OrderStatusMessage message) {
        String destination = "/topic/orders/" + message.getRestaurantId();
        messagingTemplate.convertAndSend(destination, message);
    }
}
