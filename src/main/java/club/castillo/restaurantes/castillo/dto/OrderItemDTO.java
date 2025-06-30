package club.castillo.restaurantes.castillo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {
    private Long id;
    private Long menuItemId;
    private String name;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal subtotal;
} 