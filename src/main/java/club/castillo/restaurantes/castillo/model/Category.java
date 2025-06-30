package club.castillo.restaurantes.castillo.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CategoryType type; // Por ejemplo, BEVERAGE

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "category")
    private List<Beverage> beverages = new ArrayList<>();
}
