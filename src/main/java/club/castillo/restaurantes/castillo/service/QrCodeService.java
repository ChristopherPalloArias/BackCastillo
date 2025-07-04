package club.castillo.restaurantes.castillo.service;

import club.castillo.restaurantes.castillo.dto.QrCodeDTO;
import club.castillo.restaurantes.castillo.model.QrCode;
import club.castillo.restaurantes.castillo.model.Restaurant;
import club.castillo.restaurantes.castillo.model.RoleType;
import club.castillo.restaurantes.castillo.model.User;
import club.castillo.restaurantes.castillo.repository.QrCodeRepository;
import club.castillo.restaurantes.castillo.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QrCodeService {
    private final QrCodeRepository qrCodeRepository;
    private final RestaurantRepository restaurantRepository;

    @Transactional(readOnly = true)
    public List<QrCodeDTO> getAllQrCodes() {
        return qrCodeRepository.findByActiveTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public QrCodeDTO getQrCodeById(Long id) {
        QrCode qrCode = qrCodeRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("QR Code not found"));
        return convertToDTO(qrCode);
    }

    @Transactional
    public QrCodeDTO createQrCode(QrCodeDTO qrCodeDTO) {
        Restaurant restaurant = restaurantRepository.findByIdAndActiveTrue(qrCodeDTO.getRestaurantId())
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        // Verificar permisos
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        if (!currentUser.getRole().getName().equals(RoleType.OWNER) &&
            !(currentUser.getRole().getName().equals(RoleType.RESTAURANT_ADMIN) && 
              currentUser.getId().equals(restaurant.getAdmin().getId()))) {
            throw new AccessDeniedException("You don't have permission to create QR codes for this restaurant");
        }

        QrCode qrCode = QrCode.builder()
                .restaurant(restaurant)
                .active(true)
                .build();

        qrCode = qrCodeRepository.save(qrCode);
        return convertToDTO(qrCode);
    }

    @Transactional
    public QrCodeDTO updateQrCode(Long id, QrCodeDTO qrCodeDTO) {
        QrCode qrCode = qrCodeRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("QR Code not found"));

        // Verificar permisos
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        if (!currentUser.getRole().getName().equals(RoleType.OWNER) &&
            !(currentUser.getRole().getName().equals(RoleType.RESTAURANT_ADMIN) && 
              currentUser.getId().equals(qrCode.getRestaurant().getAdmin().getId()))) {
            throw new AccessDeniedException("You don't have permission to update this QR code");
        }

        qrCode.setUpdatedAt(LocalDateTime.now());

        qrCode = qrCodeRepository.save(qrCode);
        return convertToDTO(qrCode);
    }

    @Transactional
    public void deleteQrCode(Long id) {
        QrCode qrCode = qrCodeRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("QR Code not found"));

        // Verificar permisos
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        if (!currentUser.getRole().getName().equals(RoleType.OWNER) &&
            !(currentUser.getRole().getName().equals(RoleType.RESTAURANT_ADMIN) && 
              currentUser.getId().equals(qrCode.getRestaurant().getAdmin().getId()))) {
            throw new AccessDeniedException("You don't have permission to delete this QR code");
        }

        qrCode.setActive(false);
        qrCode.setUpdatedAt(LocalDateTime.now());
        qrCodeRepository.save(qrCode);
    }

    private QrCodeDTO convertToDTO(QrCode qrCode) {
        return QrCodeDTO.builder()
                .id(qrCode.getId())
                .restaurantId(qrCode.getRestaurant().getId())
                .build();
    }
} 