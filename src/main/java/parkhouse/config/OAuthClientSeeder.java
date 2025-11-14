package parkhouse.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.security.oauth2.server.authorization.client.RegisteredClient.withId;

@Configuration
public class OAuthClientSeeder {

    @Bean
    CommandLineRunner registerParkingClient(RegisteredClientRepository clients,
                                            PasswordEncoder encoder) {
        return args -> {
            if (clients.findByClientId("parking-external-machine-01") == null)
                createClient("parking-external-machine-01", clients, encoder,
                        List.of("ROLE_GATE_MACHINE", "ROLE_ENTRY_GATE_MACHINE"));

            if (clients.findByClientId("parking-internal-machine-01") == null)
                createClient("parking-internal-machine-01", clients, encoder,
                        List.of("ROLE_GATE_MACHINE", "ROLE_EXIT_GATE_MACHINE"));
        };
    }

    private void createClient(String clientId, RegisteredClientRepository clients, PasswordEncoder encoder, List<String> roles) {
        var client = withId(UUID.randomUUID().toString())
                .clientId(clientId)
                .clientSecret(encoder.encode("change_me"))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("https://insomnia.rest/callback")
                .scope(OidcScopes.OPENID)
                .clientSettings(ClientSettings.builder()
                        .setting("roles", new ArrayList<>(roles))
                        .build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofDays(7))
                        .build())
                .build();

        clients.save(client);
    }
}


