package club.castillo.restaurantes.castillo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantBeverageDTO {
    private Long restaurantId;
    private Long beverageId;
    private boolean available;
    // Puedes agregar info de la bebida si lo necesitas, ej:
    private String beverageName;
    private String beverageDescription;
    private Double beveragePrice;
    private String beverageImageBase64;

    private Long beverageCategoryId;
    private String beverageCategoryName;
}
