package club.castillo.restaurantes.castillo.model;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class RestaurantBeverageId implements Serializable {
    private Long restaurantId;
    private Long beverageId;
}
