package parkhouse.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record EntryRequest(
        @NotNull UUID entryGateId
) {}

