package parkhouse.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import java.util.UUID;

@Configuration
public class OAuthClientSeeder {

    @Bean
    CommandLineRunner registerParkingClient(RegisteredClientRepository clients,
                                            PasswordEncoder encoder) {
        return args -> {
            if (clients.findByClientId("parking-client") != null) return;

            var client = RegisteredClient.withId(UUID.randomUUID().toString())
                    .clientId("parking-client")
                    .clientSecret(encoder.encode("test123"))
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                    .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                    .redirectUri("https://insomnia.rest/callback")
                    .scope(OidcScopes.OPENID)
                    .scope("api.read")
                    .scope("api.write")
                    .build();

            clients.save(client);
        };
    }
}


