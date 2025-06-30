package club.castillo.restaurantes.castillo.service;

import club.castillo.restaurantes.castillo.dto.BeverageDTO;
import club.castillo.restaurantes.castillo.model.*;
import club.castillo.restaurantes.castillo.repository.BeverageRepository;
import club.castillo.restaurantes.castillo.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BeverageService {
    private final BeverageRepository beverageRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<BeverageDTO> getAllBeverages() {
        return beverageRepository.findByActiveTrue()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BeverageDTO getBeverageById(Long id) {
        Beverage beverage = beverageRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Bebida no encontrada"));
        return toDTO(beverage);
    }

    @Transactional
    public BeverageDTO createBeverage(BeverageDTO dto) {
        checkOwnerRole();

        // ✅ Validaciones exhaustivas
        log.info("=== INICIO CREACIÓN BEBIDA ===");
        log.info("DTO recibido: {}", dto);

        if (dto == null) {
            throw new IllegalArgumentException("El DTO no puede ser nulo");
        }

        if (dto.getCategoryId() == null) {
            log.error("CategoryId es null en el DTO");
            throw new IllegalArgumentException("El ID de categoría no puede ser nulo");
        }

        log.info("CategoryId recibido: {}", dto.getCategoryId());

        // ✅ Buscar categoría con manejo de errores mejorado
        Category category;
        try {
            category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + dto.getCategoryId()));
            log.info("Categoría encontrada: ID={}, Name={}", category.getId(), category.getName());
        } catch (Exception e) {
            log.error("Error al buscar categoría con ID {}: {}", dto.getCategoryId(), e.getMessage());
            throw e;
        }

        // ✅ Crear bebida con valores explícitos
        Beverage beverage = new Beverage();
        beverage.setName(dto.getName());
        beverage.setDescription(dto.getDescription());
        beverage.setPrice(dto.getPrice());
        beverage.setCategory(category); // ✅ Asignar categoría directamente
        beverage.setImageBase64(dto.getImageBase64());
        beverage.setAvailable(dto.isAvailable());
        beverage.setActive(true);
        beverage.setCreatedAt(LocalDateTime.now());

        // ✅ Log detallado antes de guardar
        log.info("Bebida preparada para guardar:");
        log.info("  - Nombre: {}", beverage.getName());
        log.info("  - Precio: {}", beverage.getPrice());
        log.info("  - Categoría: {}", beverage.getCategory());
        log.info("  - CategoryId: {}", beverage.getCategory() != null ? beverage.getCategory().getId() : "NULL");
        log.info("  - Active: {}", beverage.getActive());
        log.info("  - Available: {}", beverage.getAvailable());
        log.info("  - CreatedAt: {}", beverage.getCreatedAt());

        try {
            // ✅ Guardar con manejo de errores
            log.info("Intentando guardar en base de datos...");
            Beverage savedBeverage = beverageRepository.save(beverage);
            log.info("Bebida guardada exitosamente con ID: {}", savedBeverage.getId());

            BeverageDTO result = toDTO(savedBeverage);
            log.info("=== FIN CREACIÓN BEBIDA EXITOSA ===");
            return result;

        } catch (Exception e) {
            log.error("Error al guardar bebida en base de datos: {}", e.getMessage(), e);
            throw new RuntimeException("Error al guardar la bebida: " + e.getMessage(), e);
        }
    }

    @Transactional
    public BeverageDTO updateBeverage(Long id, BeverageDTO dto) {
        checkOwnerRole();

        if (dto.getCategoryId() == null) {
            throw new IllegalArgumentException("El ID de categoría no puede ser nulo");
        }

        Beverage beverage = beverageRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Bebida no encontrada"));
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        beverage.setName(dto.getName());
        beverage.setDescription(dto.getDescription());
        beverage.setPrice(dto.getPrice());
        beverage.setCategory(category);
        beverage.setImageBase64(dto.getImageBase64());
        beverage.setAvailable(dto.isAvailable());
        beverage.setUpdatedAt(LocalDateTime.now());

        return toDTO(beverageRepository.save(beverage));
    }

    @Transactional
    public void deleteBeverage(Long id) {
        checkOwnerRole();
        Beverage beverage = beverageRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Bebida no encontrada"));
        beverage.setActive(false);
        beverage.setUpdatedAt(LocalDateTime.now());
        beverageRepository.save(beverage);
    }

    private BeverageDTO toDTO(Beverage b) {
        if (b == null) {
            return null;
        }

        return BeverageDTO.builder()
                .id(b.getId())
                .name(b.getName())
                .description(b.getDescription())
                .price(b.getPrice())
                .categoryId(b.getCategory() != null ? b.getCategory().getId() : null)
                .categoryName(b.getCategory() != null ? b.getCategory().getName() : null)
                .imageBase64(b.getImageBase64())
                .available(b.getAvailable() != null ? b.getAvailable() : true)
                .build();
    }

    private void checkOwnerRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        if (!user.getRole().getName().equals(RoleType.OWNER)) {
            throw new AccessDeniedException("Solo OWNER puede gestionar bebidas");
        }
    }
}