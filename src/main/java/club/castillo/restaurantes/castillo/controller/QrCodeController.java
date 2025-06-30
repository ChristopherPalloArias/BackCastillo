package club.castillo.restaurantes.castillo.controller;

import club.castillo.restaurantes.castillo.dto.QrCodeDTO;
import club.castillo.restaurantes.castillo.service.QrCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/qr-codes")
@RequiredArgsConstructor
public class QrCodeController {
    private final QrCodeService qrCodeService;

    @GetMapping
    public ResponseEntity<List<QrCodeDTO>> getAllQrCodes() {
        return ResponseEntity.ok(qrCodeService.getAllQrCodes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<QrCodeDTO> getQrCodeById(@PathVariable Long id) {
        return ResponseEntity.ok(qrCodeService.getQrCodeById(id));
    }

    @PostMapping
    public ResponseEntity<QrCodeDTO> createQrCode(@RequestBody QrCodeDTO qrCodeDTO) {
        return ResponseEntity.ok(qrCodeService.createQrCode(qrCodeDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<QrCodeDTO> updateQrCode(@PathVariable Long id, @RequestBody QrCodeDTO qrCodeDTO) {
        return ResponseEntity.ok(qrCodeService.updateQrCode(id, qrCodeDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQrCode(@PathVariable Long id) {
        qrCodeService.deleteQrCode(id);
        return ResponseEntity.ok().build();
    }
} 