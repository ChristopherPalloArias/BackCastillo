package club.castillo.restaurantes.castillo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ZoneDTO {
    private Long id;
    private String name;
    private String description;
    private String createdAt;
    private boolean active;
}

