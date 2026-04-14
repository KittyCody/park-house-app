package parkhouse.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ParkingSettingsChangeHoursRequest(
        @Min(0) @Max(24) int openingHour,
        @Min(0) @Max(24) int closingHour) {
}
