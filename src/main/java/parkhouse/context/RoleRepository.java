package parkhouse.context;

import org.springframework.data.jpa.repository.JpaRepository;
import parkhouse.domain.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findRoleByName(String name);
    boolean existsByName(String name);
}
