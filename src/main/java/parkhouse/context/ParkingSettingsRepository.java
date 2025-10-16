package parkhouse.context;

import org.springframework.data.jpa.repository.JpaRepository;
import parkhouse.domain.ParkingSettings;

import java.util.Optional;

public interface ParkingSettingsRepository extends JpaRepository<ParkingSettings, Long> {
    Optional<ParkingSettings> findTopByOrderByIdDesc();
}
