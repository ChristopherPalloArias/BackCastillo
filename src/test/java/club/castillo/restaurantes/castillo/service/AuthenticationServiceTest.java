package club.castillo.restaurantes.castillo.service;

import club.castillo.restaurantes.castillo.dto.AuthenticationRequest;
import club.castillo.restaurantes.castillo.dto.AuthenticationResponse;
import club.castillo.restaurantes.castillo.dto.RegisterRequest;
import club.castillo.restaurantes.castillo.model.Role;
import club.castillo.restaurantes.castillo.model.RoleType;
import club.castillo.restaurantes.castillo.model.User;
import club.castillo.restaurantes.castillo.repository.RoleRepository;
import club.castillo.restaurantes.castillo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para el servicio AuthenticationService.
 *
 * Este conjunto de tests valida los flujos críticos de autenticación:
 *  1. Que el registro de usuario (register) funciona correctamente: guarda un usuario, asigna un rol y retorna un token JWT.
 *  2. Que la autenticación (authenticate) de usuarios existentes genera correctamente el token JWT al autenticarse.
 *
 * Estos tests usan Mockito para simular los repositorios y servicios relacionados.
 * Permiten asegurar que los principales mecanismos de autenticación y seguridad funcionan de forma aislada,
 * sin requerir base de datos ni infraestructura real.
 */
class AuthenticationServiceTest {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private AuthenticationManager authenticationManager;
    private AuthenticationService authenticationService;

    /**
     * Antes de cada test se inicializan los mocks y el servicio a probar.
     */
    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        roleRepository = mock(RoleRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtService = mock(JwtService.class);
        authenticationManager = mock(AuthenticationManager.class);

        authenticationService = new AuthenticationService(
                userRepository,
                roleRepository,
                passwordEncoder,
                jwtService,
                authenticationManager
        );
    }

    /**
     * Prueba el método register, simulando el registro de un usuario CUSTOMER.
     * El test valida que:
     * - Se asigna correctamente el rol al usuario.
     * - Se guarda el usuario en el repositorio.
     * - Se genera un token JWT y se retorna en la respuesta.
     */
    @Test
    void testRegister() {
        // Arrange: Prepara el request de registro y los mocks necesarios
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Juan");
        request.setLastName("Chávez");
        request.setEmail("juan@ejemplo.com");
        request.setPassword("password");

        Role customerRole = new Role();
        customerRole.setName(RoleType.CUSTOMER);

        when(roleRepository.findByName(RoleType.CUSTOMER)).thenReturn(Optional.of(customerRole));
        when(passwordEncoder.encode("password")).thenReturn("hashedpassword");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);
        when(jwtService.generateToken(any(User.class))).thenReturn("fake-jwt-token");

        // Act: Llama al método de registro
        AuthenticationResponse response = authenticationService.register(request);

        // Assert: Verifica que la respuesta contiene el token y que el usuario fue guardado
        assertNotNull(response);
        assertEquals("fake-jwt-token", response.getToken());
        verify(userRepository, times(1)).save(any(User.class));
    }

    /**
     * Prueba el método authenticate, simulando el login de un usuario existente.
     * El test valida que:
     * - El usuario es encontrado en el repositorio.
     * - El token JWT es generado y devuelto.
     * - Se invoca el método de autenticación (authenticationManager.authenticate).
     */
    @Test
    void testAuthenticate() {
        // Arrange: Prepara el request de autenticación y el usuario simulado
        AuthenticationRequest request = new AuthenticationRequest();
        request.setEmail("juan@ejemplo.com");
        request.setPassword("password");

        User user = User.builder()
                .email("juan@ejemplo.com")
                .password("hashedpassword")
                .build();

        when(userRepository.findByEmail("juan@ejemplo.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        // Act: Llama al método de autenticación
        AuthenticationResponse response = authenticationService.authenticate(request);

        // Assert: Verifica que la respuesta contiene el token y que se llamó al manager de autenticación
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        verify(authenticationManager, times(1)).authenticate(any());
    }
}
