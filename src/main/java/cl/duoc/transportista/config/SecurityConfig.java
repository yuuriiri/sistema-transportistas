package cl.duoc.transportista.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuracion de seguridad del backend (Spring Security 6 / Spring Boot 3.5).
 *
 * El backend se comporta como un OAuth2 Resource Server: NO emite tokens, solo
 * VALIDA el JWT que llega en la cabecera "Authorization: Bearer ...". El JWT es
 * emitido por el IDaaS (Azure AD B2C / Microsoft Entra) y validado tambien por el
 * API Gateway. Aqui se valida de nuevo (defensa en profundidad) y ademas se aplican
 * los 2 roles exigidos en el caso:
 *
 *   - Rol "Guias.Descargar" : SOLO puede usar GET /api/guias/{id}/descargar.
 *   - Rol "Guias.Gestion"   : puede usar el resto de los endpoints (y tambien descargar).
 *
 * Sin token valido => 401 Unauthorized.
 * Token valido pero sin el rol adecuado => 403 Forbidden.
 */
@Configuration
public class SecurityConfig {

    @Value("${app.security.jwk-set-uri}")
    private String jwkSetUri;

    @Value("${app.security.audience:}")
    private String audience;

    @Value("${app.security.issuer:}")
    private String issuer;

    @Value("${app.security.role-descarga:Guias.Descargar}")
    private String roleDescarga;

    @Value("${app.security.role-gestion:Guias.Gestion}")
    private String roleGestion;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // API REST sin estado: no usamos sesiones ni CSRF basado en cookies.
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Endpoint de descarga: solo el rol de descarga o el de gestion.
                .requestMatchers(HttpMethod.GET, "/api/guias/*/descargar")
                    .hasAnyAuthority(roleDescarga, roleGestion)
                // Cualquier otro endpoint: solo el rol de gestion.
                .anyRequest().hasAuthority(roleGestion)
            )
            // Activa el modo Resource Server validando JWT con nuestro decoder y converter.
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
        return http.build();
    }

    /**
     * Decoder que descarga las llaves publicas (JWKS) del IDaaS y valida:
     *  - firma de la llave (siempre)
     *  - expiracion / "not before" (siempre)
     *  - audience (solo si app.security.audience esta definido)
     *  - issuer  (solo si app.security.issuer esta definido)
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();

        List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();
        validators.add(new JwtTimestampValidator());

        if (issuer != null && !issuer.isBlank()) {
            validators.add(new JwtIssuerValidator(issuer));
        }
        if (audience != null && !audience.isBlank()) {
            validators.add(new AudienceValidator(audience));
        }

        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(validators));
        return decoder;
    }

    /**
     * Convierte los claims del JWT en authorities de Spring:
     *  - "roles" (array)  -> App Roles de Azure (ej: "Guias.Gestion").
     *  - "scp"/"scope"    -> scopes OAuth2 separados por espacio.
     */
    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(SecurityConfig::extractAuthorities);
        return converter;
    }

    private static Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // App Roles (claim "roles" como lista).
        Object roles = jwt.getClaim("roles");
        if (roles instanceof List<?> list) {
            for (Object r : list) {
                if (r != null) {
                    authorities.add(new SimpleGrantedAuthority(r.toString()));
                }
            }
        }

        // Scopes (Azure entrega "scp"; otros IDaaS usan "scope"). String separado por espacios.
        Object scp = jwt.getClaim("scp");
        if (scp == null) {
            scp = jwt.getClaim("scope");
        }
        if (scp instanceof String s && !s.isBlank()) {
            for (String sc : s.trim().split("\\s+")) {
                authorities.add(new SimpleGrantedAuthority(sc));
            }
        }

        return authorities;
    }

    /**
     * Valida que el claim "aud" del token contenga la audience esperada
     * (normalmente el Application ID / Client ID de la API expuesta).
     */
    static class AudienceValidator implements OAuth2TokenValidator<Jwt> {
        private final String expectedAudience;

        AudienceValidator(String expectedAudience) {
            this.expectedAudience = expectedAudience;
        }

        @Override
        public OAuth2TokenValidatorResult validate(Jwt jwt) {
            if (jwt.getAudience() != null && jwt.getAudience().contains(expectedAudience)) {
                return OAuth2TokenValidatorResult.success();
            }
            OAuth2Error error = new OAuth2Error(
                    "invalid_audience",
                    "La audience requerida no esta presente en el token",
                    null);
            return OAuth2TokenValidatorResult.failure(error);
        }
    }
}