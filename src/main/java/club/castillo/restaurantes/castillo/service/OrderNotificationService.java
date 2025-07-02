package club.castillo.restaurantes.castillo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void notifyClient(Long orderId, String status, String message) {
        messagingTemplate.convertAndSend(
                "/topic/orders/client/" + orderId,
                new NotificationMessage(status, message)
        );
    }
    public void notifyRestaurant(Long restaurantId, String status, String message) {
        messagingTemplate.convertAndSend(
                "/topic/orders/restaurant/" + restaurantId,
                new NotificationMessage(status, message)
        );
    }
    public record NotificationMessage(String status, String message) {}
}
