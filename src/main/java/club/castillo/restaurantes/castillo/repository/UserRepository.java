package club.castillo.restaurantes.castillo.repository;

import club.castillo.restaurantes.castillo.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRoleName(String name);
    Optional<User> findFirstByRoleName(String roleName);
    Page<User> findByRoleNameAndActiveTrue(String roleName, Pageable pageable);
    Page<User> findByActiveTrue(Pageable pageable);
    // Métodos con búsqueda
    @Query("SELECT u FROM User u WHERE u.active = true AND " +
            "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> findByActiveTrueAndSearchTerm(@Param("search") String search, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.active = true AND u.role.name = :roleName AND " +
            "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> findByRoleNameAndActiveTrueAndSearchTerm(@Param("roleName") String roleName,
                                                        @Param("search") String search,
                                                        Pageable pageable);
} 