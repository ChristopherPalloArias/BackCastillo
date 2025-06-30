package club.castillo.restaurantes.castillo.controller;

import club.castillo.restaurantes.castillo.dto.UserDTO;
import club.castillo.restaurantes.castillo.model.Role;
import club.castillo.restaurantes.castillo.model.User;
import club.castillo.restaurantes.castillo.repository.RestaurantRepository;
import club.castillo.restaurantes.castillo.repository.RoleRepository;
import club.castillo.restaurantes.castillo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final RestaurantRepository restaurantRepository;

    // --- Obtener usuario autenticado (con imagen base64) ---
    @GetMapping("/me")
    @Transactional(readOnly = true)
    public ResponseEntity<UserDTO> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return ResponseEntity.ok(toDTO(user, true));
    }

    // --- Listar usuarios CON PAGINACIÓN (sin imagen base64) ---
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getUsers(
            @RequestParam(name = "role", required = false) String role,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "search", required = false) String search) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> userPage;

        if (role != null && search != null) {
            userPage = userRepository.findByRoleNameAndActiveTrueAndSearchTerm(role, search, pageable);
        } else if (role != null) {
            userPage = userRepository.findByRoleNameAndActiveTrue(role, pageable);
        } else if (search != null) {
            userPage = userRepository.findByActiveTrueAndSearchTerm(search, pageable);
        } else {
            userPage = userRepository.findByActiveTrue(pageable);
        }

        List<UserDTO> users = userPage.getContent().stream()
                .map(u -> toDTO(u, false)) // SIN imágenes para el listado
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("users", users);
        response.put("totalElements", userPage.getTotalElements());
        response.put("totalPages", userPage.getTotalPages());
        response.put("currentPage", userPage.getNumber());
        response.put("pageSize", userPage.getSize());
        response.put("hasNext", userPage.hasNext());
        response.put("hasPrevious", userPage.hasPrevious());

        return ResponseEntity.ok(response);
    }

    // --- NUEVO: Endpoint para obtener imágenes de usuarios específicos ---
    @GetMapping("/images")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<Long, String>> getUserImages(@RequestParam List<Long> userIds) {
        List<User> users = userRepository.findAllById(userIds);

        Map<Long, String> images = users.stream()
                .filter(user -> user.getProfileImageBase64() != null)
                .collect(Collectors.toMap(
                        User::getId,
                        User::getProfileImageBase64
                ));

        return ResponseEntity.ok(images);
    }

    // --- Crear usuario ---
    @PostMapping
    @Transactional
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO) {
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            return ResponseEntity.badRequest().build();
        }
        Role role = roleRepository.findByNameAndActiveTrue(userDTO.getRole())
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        User user = User.builder()
                .firstName(userDTO.getFirstName())
                .lastName(userDTO.getLastName())
                .email(userDTO.getEmail())
                .password(passwordEncoder.encode(
                        userDTO.getPassword() != null && !userDTO.getPassword().isBlank()
                                ? userDTO.getPassword()
                                : "password"
                ))
                .role(role)
                .profileImageBase64(userDTO.getProfileImageBase64())
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        user = userRepository.save(user);
        return ResponseEntity.ok(toDTO(user, true));
    }

    // --- Editar usuario (con imagen) ---
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Role role = roleRepository.findByNameAndActiveTrue(userDTO.getRole())
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        if ("OWNER".equals(user.getRole().getName())) {
            user.setFirstName(userDTO.getFirstName());
            user.setLastName(userDTO.getLastName());
            user.setEmail(userDTO.getEmail());
            user.setRole(role);
            user.setProfileImageBase64(userDTO.getProfileImageBase64());
            user.setUpdatedAt(LocalDateTime.now());
        } else {
            user.setFirstName(userDTO.getFirstName());
            user.setLastName(userDTO.getLastName());
            user.setEmail(userDTO.getEmail());
            user.setRole(role);
            user.setProfileImageBase64(userDTO.getProfileImageBase64());
            user.setUpdatedAt(LocalDateTime.now());
            if (userDTO.getPassword() != null && !userDTO.getPassword().isBlank()) {
                user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            }
        }

        user = userRepository.save(user);
        return ResponseEntity.ok(toDTO(user, true));
    }

    // --- Soft-delete usuario ---
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if ("OWNER".equals(user.getRole().getName())) {
            return ResponseEntity.status(403).build();
        }
        user.setActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    // --- Actualizar perfil del usuario autenticado (con imagen) ---
    @PutMapping("/me")
    @Transactional
    public ResponseEntity<UserDTO> updateCurrentUser(@RequestBody UserDTO userDTO, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String role = user.getRole().getName();
        if ("OWNER".equals(role)) {
            return ResponseEntity.status(403).build(); // O con mensaje personalizado si quieres
        }

        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setProfileImageBase64(userDTO.getProfileImageBase64());
        user.setUpdatedAt(LocalDateTime.now());

        user = userRepository.save(user);
        return ResponseEntity.ok(toDTO(user, true));
    }


    // --- Utilidad: convertir entidad a DTO, según si se necesita imagen o no ---
    private UserDTO toDTO(User user, boolean includeImage) {
        Long restaurantId = null;
        if ("RESTAURANT_ADMIN".equals(user.getRole().getName())) {
            var restaurant = restaurantRepository.findByAdminIdAndActiveTrue(user.getId());
            if (restaurant.isPresent()) {
                restaurantId = restaurant.get().getId();
            }
        }
        return new UserDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole().getName(),
                includeImage ? user.getProfileImageBase64() : null,
                null,
                restaurantId
        );
    }
    @GetMapping("/all")
    @Transactional(readOnly = true)
    public ResponseEntity<List<UserDTO>> getAllUsersByRole(@RequestParam(name = "role", required = false) String role) {
        List<User> users;
        if (role != null) {
            users = userRepository.findByRoleNameAndActiveTrue(role, Pageable.unpaged()).getContent();
        } else {
            users = userRepository.findByActiveTrue(Pageable.unpaged()).getContent();
        }
        List<UserDTO> dtos = users.stream().map(u -> toDTO(u, false)).toList();
        return ResponseEntity.ok(dtos);
    }

    // --- Cambiar contraseña del usuario autenticado ---
    @PutMapping("/change-password")
    @Transactional
    public ResponseEntity<?> changePassword(
            @RequestBody Map<String, String> payload,
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("No autenticado");
        }
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String role = user.getRole().getName();
        if ("OWNER".equals(role)) {
            return ResponseEntity.status(403).body("No autorizado para cambiar contraseña");
        }

        String oldPassword = payload.get("oldPassword");
        String newPassword = payload.get("newPassword");

        if (oldPassword == null || newPassword == null) {
            return ResponseEntity.badRequest().body("Campos obligatorios faltantes");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return ResponseEntity.status(403).body("La contraseña actual no es correcta");
        }
        if (oldPassword.equals(newPassword)) {
            return ResponseEntity.badRequest().body("La nueva contraseña no puede ser igual a la anterior");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return ResponseEntity.ok().body("Contraseña actualizada");
    }

}