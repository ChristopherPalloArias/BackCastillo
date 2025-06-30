package club.castillo.restaurantes.castillo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantDTO {
    private Long id;
    private String name;
    private String description;
    private Long adminId;
    private Long zoneId;
    private String status;
    private boolean active;
    private String imageBase64;
} 