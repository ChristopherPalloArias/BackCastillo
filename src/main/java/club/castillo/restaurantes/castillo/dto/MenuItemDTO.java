package club.castillo.restaurantes.castillo.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemDTO {
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String name;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String description;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
    private BigDecimal price;

    @NotNull(message = "La categoría es obligatoria")
    private Long categoryId;

    private String categoryName;

    @NotNull(message = "El restaurante es obligatorio")
    private Long restaurantId;

    private String restaurantName;

    private String imageBase64;

    @Builder.Default
    private boolean active = true;

    @Builder.Default
    private boolean available = true;
}

