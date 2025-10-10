package parkhouse.config;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class SecurityConfig {

    @Value("${app.jwt.hmac-secret}")
    private String hmacSecretBase64;

    @Value("${app.issuer}")
    private String issuer;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return org.springframework.security.crypto.factory.PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    private SecretKey hmacKey() {
        byte[] bytes = java.util.Base64.getDecoder().decode(hmacSecretBase64);
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    @Order(0)
    SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        var as = OAuth2AuthorizationServerConfigurer.authorizationServer();
        var endpoints = as.getEndpointsMatcher();

        http.securityMatcher(endpoints)
                .authorizeHttpRequests(a -> a.anyRequest().authenticated())
                .csrf(c -> c.ignoringRequestMatchers(endpoints))
                .with(as, cfg -> cfg.oidc(Customizer.withDefaults()));

        return http.build();
    }

    @Bean
    @Order(1)
    SecurityFilterChain appSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**", "/actuator/**", "/login")
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/auth/register"))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/register", "/actuator/health").permitAll()
                        .requestMatchers("/api/**").hasAuthority("SCOPE_api.read")
                        .anyRequest().authenticated()
                )
                .formLogin(Customizer.withDefaults())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );
        return http.build();
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer(issuer)
                .build();
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        // Advertise HS256 on the octet key
        OctetSequenceKey hmac = new OctetSequenceKey.Builder(hmacKey())
                .keyID("hmac-key-id")
                .algorithm(JWSAlgorithm.HS256)
                .build();
        return new ImmutableJWKSet<>(new JWKSet(hmac));
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        // Resource server side: validate HS256 with the same secret
        return NimbusJwtDecoder.withSecretKey(hmacKey()).build();
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
        return context -> {
            // Force HS256 only for access tokens
            if ("access_token".equals(context.getTokenType().getValue())) {
                context.getJwsHeader().algorithm(MacAlgorithm.HS256);
            }

            var authorities = context.getPrincipal().getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());

            context.getClaims().claim("roles", authorities);
        };
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(this::extractAuthorities);
        return converter;
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        final var rolesObj = jwt.getClaim("roles");

        if (rolesObj instanceof Collection<?> col) {
            return col.stream()
                    .map(Object::toString)
                    .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                    .map(org.springframework.security.core.authority.SimpleGrantedAuthority::new)
                    .collect(Collectors.toSet());
        }
        return Set.of();
    }
}
