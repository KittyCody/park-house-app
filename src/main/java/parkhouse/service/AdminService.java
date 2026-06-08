package parkhouse.service;

import org.springframework.stereotype.Service;
import parkhouse.context.FloorRepository;
import parkhouse.context.ParkingSettingsRepository;
import parkhouse.context.TicketRepository;
import parkhouse.dto.FloorResponse;
import parkhouse.dto.ParkingSettingsChangeHoursRequest;
import parkhouse.dto.ParkingSettingsChangePriceRequest;

import java.util.List;

@Service
public class AdminService {
    private final ParkingSettingsRepository parkingSettings;
    private final FloorRepository floors;
    private final TicketRepository tickets;

    public AdminService(ParkingSettingsRepository parkingSettings, FloorRepository floors, TicketRepository tickets) {
        this.parkingSettings = parkingSettings;
        this.floors = floors;
        this.tickets = tickets;
    }

    public List<FloorResponse> getFloors() {
        return floors.findAll().stream()
                .map(floor -> FloorResponse.of(floor, tickets.countByFloor_IdAndTimeOfExitIsNull(floor.getId())))
                .toList();
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
