package club.castillo.restaurantes.castillo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para JwtService.
 *
 * Este conjunto de tests valida el ciclo básico de generación y validación de tokens JWT:
 *  1. Que se puede generar un token válido para un usuario simulado.
 *  2. Que se puede extraer el username correctamente desde el token.
 *  3. Que la validación del token funciona correctamente (true/false según usuario y expiración).
 *
 * Se utiliza un secretKey de prueba para no depender del archivo de configuración.
 */
class JwtServiceTest {

    private JwtService jwtService;
    private final String secretKey = "bXktdGVzdC1zZWNyZXQta2V5LWZvci1qd3Qtc2lnbmVkIQ=="; // "my-test-secret-key-for-jwt-signed!" base64 (32 bytes)
    private final long expiration = 60_000L; // 1 minuto en ms

    /**
     * Inicializa JwtService y le inyecta el secretKey y expiration de prueba.
     */
    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", expiration);
    }

    /**
     * Prueba que se puede generar y validar correctamente un token para un usuario.
     */
    @Test
    void testGenerateAndValidateToken() {
        // Arrange: Usuario simulado
        UserDetails userDetails = User.withUsername("testuser").password("pass").authorities(Collections.emptyList()).build();

        // Act: Genera el token y lo valida
        String token = jwtService.generateToken(userDetails);

        // Assert: El token NO debe ser nulo o vacío, y debe ser válido para el usuario
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    /**
     * Prueba que se puede extraer el username original desde el token generado.
     */
    @Test
    void testExtractUsername() {
        // Arrange: Usuario y token
        UserDetails userDetails = User.withUsername("admin").password("x").authorities(Collections.emptyList()).build();
        String token = jwtService.generateToken(userDetails);

        // Act: Extrae el username
        String username = jwtService.extractUsername(token);

        // Assert: El username extraído debe coincidir con el original
        assertEquals("admin", username);
    }

    /**
     * Prueba que un token generado para un usuario NO es válido para otro usuario.
     */
    @Test
    void testIsTokenValidForDifferentUser() {
        UserDetails user1 = User.withUsername("user1").password("1").authorities(Collections.emptyList()).build();
        UserDetails user2 = User.withUsername("user2").password("2").authorities(Collections.emptyList()).build();

        String token = jwtService.generateToken(user1);

        assertFalse(jwtService.isTokenValid(token, user2));
    }
}
