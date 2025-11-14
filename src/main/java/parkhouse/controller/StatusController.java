package parkhouse.controller;

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
    private final ParkingService parkingService;

    public StatusController(ParkingService parkingService) {
        this.parkingService = parkingService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ENTRY_GATE_MACHINE')")
    public ResponseEntity<StatusResponse> getCurrentStatus() {
        var status = parkingService.getStatus();

        return ResponseEntity.ok(status);
    }
}
