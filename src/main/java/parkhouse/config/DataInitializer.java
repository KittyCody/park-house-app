package parkhouse.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import parkhouse.context.FloorRepository;
import parkhouse.context.ParkingSettingsRepository;
import parkhouse.context.RoleRepository;
import parkhouse.context.UserRepository;
import parkhouse.domain.Floor;
import parkhouse.domain.ParkingSettings;
import parkhouse.domain.Role;
import parkhouse.domain.User;

import java.time.LocalDateTime;
import java.util.Set;

@Configuration
public class DataInitializer {
    private final String adminUsername = "admin_ph";
    private final RoleRepository roles;
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final ParkingSettingsRepository parkingSettings;
    private final FloorRepository floors;

    public DataInitializer(RoleRepository roles, UserRepository users, PasswordEncoder passwordEncoder, ParkingSettingsRepository parkingSettings, FloorRepository floors) {
        this.roles = roles;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.parkingSettings = parkingSettings;
        this.floors = floors;
    }

    @Bean
    CommandLineRunner seed() {
        return args -> {
            ensureParkSettings();
            ensureFloor();
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

    private void ensureParkSettings() {
        if (parkingSettings.existsById(1L)) return;

        parkingSettings.save(ParkingSettings.createDefault());
    }

    private void ensureFloor() {
        if (floors.existsById(1)) return;

        var floor = new Floor(200, LocalDateTime.now());
        floors.save(floor);
    }

    private Role ensureRoleExists(String roleName) {
        return roles.findRoleByName(roleName)
                .orElseGet(() -> roles.save(new Role(roleName)));
    }
}