package club.castillo.restaurantes.castillo.controller;

import club.castillo.restaurantes.castillo.dto.AuthenticationRequest;
import club.castillo.restaurantes.castillo.dto.AuthenticationResponse;
import club.castillo.restaurantes.castillo.dto.RegisterRequest;
import club.castillo.restaurantes.castillo.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(service.authenticate(request));
    }

    @PostMapping("/guest-login")
    public ResponseEntity<AuthenticationResponse> guestLogin() {
        return ResponseEntity.ok(service.authenticateAsGuest());
    }

}
