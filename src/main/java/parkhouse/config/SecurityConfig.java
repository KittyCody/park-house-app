package parkhouse.config;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Configuration
public class SecurityConfig {

    @Value("${app.issuer}")
    private String issuer;
    @Value("${app.keystore.location}")
    private Resource keystoreLocation;
    @Value("${app.keystore.password}")
    private String keystorePassword;
    @Value("${app.keystore.key-alias}")
    private String keyAlias;
    @Value("${app.keystore.key-password}")
    private String keyPassword;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return org.springframework.security.crypto.factory.PasswordEncoderFactories.createDelegatingPasswordEncoder();
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
                        // allow POST create with api.write, reads with api.read
                        .requestMatchers(HttpMethod.POST, "/api/tickets/entries").hasAuthority("SCOPE_api.write")
                        .requestMatchers(HttpMethod.GET, "/api/**").hasAuthority("SCOPE_api.read")
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
    public JWKSource<SecurityContext> jwkSource() throws Exception {
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(keystoreLocation.getInputStream(), keystorePassword.toCharArray());
        PrivateKeyEntry entry = (PrivateKeyEntry) ks.getEntry(
                keyAlias,
                new KeyStore.PasswordProtection(keyPassword.toCharArray())
        );

        RSAPrivateKey privateKey = (RSAPrivateKey) entry.getPrivateKey();
        RSAPublicKey  publicKey  = (RSAPublicKey)  entry.getCertificate().getPublicKey();

        RSAKey rsa = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID("kid-" + UUID.randomUUID())
                .algorithm(JWSAlgorithm.RS256)
                .build();

        return new ImmutableJWKSet<>(new JWKSet(rsa));
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(issuer + "/oauth2/jwks").build();
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
        return context -> {
            var authorities = context.getPrincipal().getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());
            context.getClaims().claim("roles", authorities);
        };
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        var converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(this::mergeScopesAndRoles);
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

    private Collection<GrantedAuthority> mergeScopesAndRoles(Jwt jwt) {
        var scopesConv = new JwtGrantedAuthoritiesConverter(); // default prefix SCOPE_
        var authorities = new java.util.HashSet<>(scopesConv.convert(jwt));

        Object rolesObj = jwt.getClaim("roles");
        if (rolesObj instanceof Collection<?> col) {
            authorities.addAll(col.stream()
                    .map(Object::toString)
                    .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toSet()));
        }
        return authorities;
    }
}
