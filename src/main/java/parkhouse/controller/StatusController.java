package parkhouse.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import parkhouse.dto.StatusResponse;
import parkhouse.service.ParkingService;

@RestController
@RequestMapping("/api/v1/parking-statuses/current")
public class StatusController {

    private static final Logger log = LoggerFactory.getLogger(StatusController.class);

    private final ParkingService parkingService;

    public StatusController(ParkingService parkingService) {
        this.parkingService = parkingService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ENTRY_GATE_MACHINE')")
    public ResponseEntity<StatusResponse> getCurrentStatus() {

        log.info("GET /api/v1/parking-statuses/current called");

        var status = parkingService.getStatus();

        log.info("Parking status returned: availableSpaces={}, isOperational={}",
                status.availableSpaces(), status.isOperational());

        return ResponseEntity.ok(status);
    }
}
