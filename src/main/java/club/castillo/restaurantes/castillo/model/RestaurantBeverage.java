package club.castillo.restaurantes.castillo.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "restaurant_beverages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantBeverage {

    @EmbeddedId
    private RestaurantBeverageId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("restaurantId")
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("beverageId")
    @JoinColumn(name = "beverage_id")
    private Beverage beverage;

    @Column(name = "available", nullable = false)
    @Builder.Default
    private boolean available = true;
}
