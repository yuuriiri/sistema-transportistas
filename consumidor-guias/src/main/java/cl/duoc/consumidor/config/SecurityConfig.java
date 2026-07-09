package cl.duoc.consumidor.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
 * Seguridad del microservicio Consumidor.
 * Todos los endpoints requieren el rol "Guias.Gestion" (solo gestion puede
 * consumir y ver las guias procesadas).
 */
@Configuration
public class SecurityConfig {

    @Value("${app.security.jwk-set-uri}")
    private String jwkSetUri;

    @Value("${app.security.audience:}")
    private String audience;

    @Value("${app.security.issuer:}")
    private String issuer;

    @Value("${app.security.role-gestion:Guias.Gestion}")
    private String roleGestion;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Todos los endpoints del consumidor requieren rol de gestion
                .anyRequest().hasAuthority(roleGestion)
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
        return http.build();
    }

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

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(SecurityConfig::extractAuthorities);
        return converter;
    }

    private static Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        Object roles = jwt.getClaim("roles");
        if (roles instanceof List<?> list) {
            for (Object r : list) {
                if (r != null) {
                    authorities.add(new SimpleGrantedAuthority(r.toString()));
                }
            }
        }

        Object scp = jwt.getClaim("scp");
        if (scp == null) scp = jwt.getClaim("scope");
        if (scp instanceof String s && !s.isBlank()) {
            for (String sc : s.trim().split("\\s+")) {
                authorities.add(new SimpleGrantedAuthority(sc));
            }
        }

        return authorities;
    }

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
            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_audience", "Audience requerida no presente", null));
        }
    }
}
