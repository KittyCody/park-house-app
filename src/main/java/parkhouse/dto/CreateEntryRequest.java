package parkhouse.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateEntryRequest(
        @NotNull @Min(1) Integer floorId
) {
}