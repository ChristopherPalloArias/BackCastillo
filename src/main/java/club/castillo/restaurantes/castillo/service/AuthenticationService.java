package club.castillo.restaurantes.castillo.service;

import club.castillo.restaurantes.castillo.dto.AuthenticationRequest;
import club.castillo.restaurantes.castillo.dto.AuthenticationResponse;
import club.castillo.restaurantes.castillo.dto.RegisterRequest;
import club.castillo.restaurantes.castillo.model.Role;
import club.castillo.restaurantes.castillo.model.RoleType;
import club.castillo.restaurantes.castillo.model.User;
import club.castillo.restaurantes.castillo.repository.RoleRepository;
import club.castillo.restaurantes.castillo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegisterRequest request) {
        Role customerRole = roleRepository.findByName(RoleType.CUSTOMER)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(customerRole)
                .active(true)
                .build();

        userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }
    public AuthenticationResponse authenticateAsGuest() {
        User invited = userRepository.findByEmail("invitado@club.com")
                .orElseGet(() -> userRepository.findFirstByRoleName(RoleType.INVITED)
                        .orElseThrow(() -> new RuntimeException("Usuario invitado no existe")));
        String jwtToken = jwtService.generateToken(invited);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .role(invited.getRole().getName())
                .build();
    }
} 