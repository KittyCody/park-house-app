package parkhouse.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import parkhouse.dto.ParkingSettingsChangeHoursRequest;
import parkhouse.dto.ParkingSettingsChangePriceRequest;
import parkhouse.service.AdminService;


@RestController
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/api/v1/admin/settings/working-hours")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateWorkingHours(@RequestBody
                                                ParkingSettingsChangeHoursRequest payload) {
        adminService.ChangeWorkingHours(payload);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/v1/admin/settings/price")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updatePrice(@RequestBody
                                         ParkingSettingsChangePriceRequest payload) {
        adminService.ChangePrice(payload);
        return ResponseEntity.noContent().build();
    }


}
