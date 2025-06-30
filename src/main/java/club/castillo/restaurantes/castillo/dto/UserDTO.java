package club.castillo.restaurantes.castillo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private String profileImageBase64;
    private String password; // Solo para creación/edición, nunca devolver en GET
    private Long restauranteId;
}
