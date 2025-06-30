package club.castillo.restaurantes.castillo.config;

import club.castillo.restaurantes.castillo.model.Role;
import club.castillo.restaurantes.castillo.model.User;
import club.castillo.restaurantes.castillo.repository.RoleRepository;
import club.castillo.restaurantes.castillo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // 1. Crear roles si no existen
        if (roleRepository.count() == 0) {
            Role ownerRole = Role.builder()
                    .name("OWNER")
                    .description("Puede gestionar todo el sistema, incluyendo bebidas")
                    .active(true)
                    .build();
            roleRepository.save(ownerRole);

            Role adminRole = Role.builder()
                    .name("RESTAURANT_ADMIN")
                    .description("Puede gestionar su restaurante asignado")
                    .active(true)
                    .build();
            roleRepository.save(adminRole);

            Role customerRole = Role.builder()
                    .name("CUSTOMER")
                    .description("Usuario regular que puede hacer pedidos")
                    .active(true)
                    .build();
            roleRepository.save(customerRole);

            // --- AGREGADO: Rol INVITED ---
            Role invitedRole = Role.builder()
                    .name("INVITED")
                    .description("Usuario invitado, acceso limitado solo para pedidos")
                    .active(true)
                    .build();
            roleRepository.save(invitedRole);

            // 2. Crear usuarios de prueba (si quieres)
            User owner = User.builder()
                    .firstName("Admin")
                    .lastName("Owner")
                    .email("owner@club.com")
                    .password(passwordEncoder.encode("password"))
                    .role(ownerRole)
                    .active(true)
                    .build();
            userRepository.save(owner);

            User restaurantAdmin = User.builder()
                    .firstName("Restaurant")
                    .lastName("Admin")
                    .email("admin@restaurant.com")
                    .password(passwordEncoder.encode("password"))
                    .role(adminRole)
                    .active(true)
                    .build();
            userRepository.save(restaurantAdmin);

            User customer = User.builder()
                    .firstName("Regular")
                    .lastName("Customer")
                    .email("customer@club.com")
                    .password(passwordEncoder.encode("password"))
                    .role(customerRole)
                    .active(true)
                    .build();
            userRepository.save(customer);

            // --- AGREGADO: Usuario invitado (no editable, acceso solo para pedidos) ---
            User invited = User.builder()
                    .firstName("Invitado")
                    .lastName("General")
                    .email("invitado@club.com")
                    .password(passwordEncoder.encode("noeditarnunca123")) // Nunca se usará, solo para cumplir constraints
                    .role(invitedRole)
                    .active(true)
                    .build();
            userRepository.save(invited);
        }
    }
}
