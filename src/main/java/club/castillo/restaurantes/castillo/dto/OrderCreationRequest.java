package club.castillo.restaurantes.castillo.dto;

import lombok.Data;
import java.util.List;

@Data
public class OrderCreationRequest {
    private List<OrderItemDTO> items;
    private List<OrderBeverageDTO> beverages;
}
