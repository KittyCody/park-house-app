package parkhouse.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import parkhouse.context.ParkingSettingsRepository;
import parkhouse.dto.StatusResponse;

import java.time.LocalDateTime;


@Service
public class ParkingService {

    private static final Logger log = LoggerFactory.getLogger(ParkingService.class);

    private final ParkingSettingsRepository parkingSettings;
    private final TicketService tickets;

    public ParkingService(ParkingSettingsRepository parkingSettings, TicketService tickets) {
        this.parkingSettings = parkingSettings;
        this.tickets = tickets;
    }

    public StatusResponse getStatus() {
        log.info("Retrieving parking status");

        var availableSpaces = tickets.getAvailableSpaces();
        log.debug("Available places : {}", availableSpaces);

        var settings = parkingSettings.findTopByOrderByIdDesc()
                .orElseThrow(() -> {
                    log.error("No parking configuration found !");
                    return new RuntimeException("No parking settings found");
                });

        var now = LocalDateTime.now().getHour();
        var isOperational = now > settings.openHour() && now < settings.closeHour();

        log.info("Parking open : {}", isOperational);

        return new StatusResponse(availableSpaces, isOperational);
    }
}
