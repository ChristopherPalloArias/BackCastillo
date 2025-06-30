package club.castillo.restaurantes.castillo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderBeverageDTO {
    private Long id;
    private Long beverageId;
    private Integer quantity;
    private Double price;
} 