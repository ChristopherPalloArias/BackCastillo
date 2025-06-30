package club.castillo.restaurantes.castillo.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // Público: login, zonas
                        .requestMatchers("/api/auth/**", "/zones/**").permitAll()

                        // Público: Solo consultas GET a restaurantes específicos
                        .requestMatchers(HttpMethod.GET, "/restaurants/public/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/menu-items/restaurant/**").permitAll()

                        // Privado: Todas las demás operaciones de restaurantes requieren autenticación
                        // (las anotaciones @PreAuthorize manejarán la autorización específica)
                        .requestMatchers("/restaurants/**").authenticated()

                        // Otras rutas privadas
                        .requestMatchers("/api/users/**").authenticated()
                        .requestMatchers("/beverages/**").authenticated()
                        .requestMatchers("/categories/**").authenticated()
                        .requestMatchers("/menu-items/**").authenticated()

                        // Cualquier otra ruta
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("*"); // Permite todo origen
        configuration.addAllowedHeader("*");        // Permite cualquier header
        configuration.addAllowedMethod("*");        // Permite cualquier método
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}