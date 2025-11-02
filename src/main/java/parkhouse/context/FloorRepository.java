package parkhouse.context;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import parkhouse.domain.Floor;

import java.util.Optional;

public interface FloorRepository extends JpaRepository<Floor, Integer> {
@Query("select sum(f.capacity) from Floor f")
Optional<Long> sumCapacity();
}
