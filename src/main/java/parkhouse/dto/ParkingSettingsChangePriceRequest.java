package parkhouse.dto;

import jakarta.validation.constraints.Positive;

public record ParkingSettingsChangePriceRequest(@Positive int pricePerHour) {
}
