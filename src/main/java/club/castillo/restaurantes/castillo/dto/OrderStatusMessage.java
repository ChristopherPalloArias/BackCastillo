package club.castillo.restaurantes.castillo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderStatusMessage {
    private Long orderId;
    private String status;
    private Long restaurantId;
    private String message;
}
