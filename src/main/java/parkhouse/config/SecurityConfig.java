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
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Collection;
import java.util.List;

@Configuration
@EnableMethodSecurity
@Profile("!test")
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
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/register", "/actuator/health").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(Customizer.withDefaults())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOriginPatterns(List.of("http://localhost:*", "http://127.0.0.1:*"));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
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
        RSAPublicKey publicKey = (RSAPublicKey) entry.getCertificate().getPublicKey();

        RSAKey rsa = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID("kid-6cc8cbf5-a83c-4253-86dc-a1c59d69e0a2")
                .algorithm(JWSAlgorithm.RS256)
                .build();

        return new ImmutableJWKSet<>(new JWKSet(rsa));
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
        return context -> {
            if (AuthorizationGrantType.CLIENT_CREDENTIALS.equals(context.getAuthorizationGrantType())) {
                RegisteredClient client = context.getRegisteredClient();
                context.getClaims().subject(client.getId());
                context.getClaims().claim("client_id", client.getClientId());

                Object rolesObj = client.getClientSettings().getSetting("roles");

                if (rolesObj instanceof List<?> roles) {
                    context.getClaims().claim("roles", roles);
                }

                return;
            }

            var roles = context.getPrincipal().getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(a -> a.startsWith("ROLE_"))
                    .toList();

            context.getClaims().claim("roles", roles);
        };
    }


    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        var converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(this::mergeScopesAndRoles);
        return converter;
    }

    private Collection<GrantedAuthority> mergeScopesAndRoles(Jwt jwt) {
        // 1) normal scopes -> SCOPE_xxx
        var scopesConv = new JwtGrantedAuthoritiesConverter();
        var authorities = new java.util.HashSet<>(scopesConv.convert(jwt));

        // 2) our "roles" claim -> ROLE_xxx
        var roles = jwt.getClaimAsStringList("roles");
        if (roles != null) {
            roles.forEach(r ->
                    authorities.add(new SimpleGrantedAuthority(r))   // r is already ROLE_...
            );
        }

        return authorities;
    }
}
