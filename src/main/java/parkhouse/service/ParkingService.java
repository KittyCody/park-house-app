package parkhouse.service;

import org.springframework.stereotype.Service;
import parkhouse.context.ParkingSettingsRepository;
import parkhouse.dto.StatusResponse;

import java.time.LocalDateTime;

@Service
public class ParkingService {
    private final ParkingSettingsRepository parkingSettings;
    private final TicketService tickets;

    public ParkingService(ParkingSettingsRepository parkingSettings, TicketService tickets) {
        this.parkingSettings = parkingSettings;
        this.tickets = tickets;
    }

    public StatusResponse getStatus() {
        var availableSpaces = tickets.getAvailableSpaces();

        var settings = parkingSettings.findTopByOrderByIdDesc()
                .orElseThrow(() -> new RuntimeException("No parking settings found"));
        var now = LocalDateTime.now().getHour();

        var isOperational = now > settings.openHour() && now < settings.closeHour();

        return new StatusResponse(availableSpaces, isOperational);
    }
}