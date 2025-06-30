package club.castillo.restaurantes.castillo.repository;

import club.castillo.restaurantes.castillo.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
    Optional<Role> findByNameAndActiveTrue(String name);
    boolean existsByName(String name);

} 