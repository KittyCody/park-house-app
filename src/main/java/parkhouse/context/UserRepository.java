package parkhouse.context;

import org.springframework.data.jpa.repository.JpaRepository;
import parkhouse.domain.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String name);
    boolean existsByUsername(String name);
}
