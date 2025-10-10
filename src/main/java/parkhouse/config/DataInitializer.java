package parkhouse.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import parkhouse.context.RoleRepository;
import parkhouse.context.UserRepository;
import parkhouse.domain.Role;
import parkhouse.domain.User;

import java.util.Set;

@Configuration
public class DataInitializer {
    private final String adminUsername = "admin_ph";
    private final RoleRepository roles;
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roles, UserRepository users, PasswordEncoder passwordEncoder) {
        this.roles = roles;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    CommandLineRunner seed() {
        return args -> {
            ensureRoleExists("ROLE_MACHINE");

            final var adminRole = ensureRoleExists("ROLE_ADMIN");

            if (users.existsByUsername(adminUsername)) {
                return;
            }

            final var passwordHash = passwordEncoder.encode("Th3@dm!HPaz#");
            final var adminUser = new User(adminUsername, passwordHash, Set.of(adminRole));

            users.save(adminUser);
        };
    }

    private Role ensureRoleExists(String roleName) {
        return roles.findRoleByName(roleName)
                .orElseGet(() -> roles.save(new Role(roleName)));
    }
}