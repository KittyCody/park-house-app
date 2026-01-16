package parkhouse.service;

import org.springframework.stereotype.Service;
import parkhouse.context.ParkingSettingsRepository;
import parkhouse.dto.ParkingSettingsChangeHoursRequest;
import parkhouse.dto.ParkingSettingsChangePriceRequest;

@Service
public class AdminService {
    private final ParkingSettingsRepository parkingSettings;

    public AdminService(ParkingSettingsRepository parkingSettings) {
        this.parkingSettings = parkingSettings;
    }

    public void ChangeWorkingHours(ParkingSettingsChangeHoursRequest payload) {
        var currentSettings = parkingSettings.findTopByOrderByIdDesc().orElseThrow();
        currentSettings.updateOperationalHours(payload.openingHour(), payload.closingHour());

        this.parkingSettings.save(currentSettings);
    }

    public void ChangePrice(ParkingSettingsChangePriceRequest payload) {
        var currentSettings = parkingSettings.findTopByOrderByIdDesc().orElseThrow();
        currentSettings.updatePrice(payload.pricePerHour());

        this.parkingSettings.save(currentSettings);
    }

}
