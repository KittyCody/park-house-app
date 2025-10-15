package parkhouse.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record TicketResponse(
        UUID id,
        UUID entryGateId,
        UUID exitGateId,
        LocalDateTime timeOfEntry,
        LocalDateTime timeOfExit
) {
    public static TicketResponse of(parkhouse.domain.Ticket t) {
        return new TicketResponse(
                t.getId(),
                t.getEntryGateId(),
                t.getExitGateId(),
                t.getTimeOfEntry(),
                t.getTimeOfExit()
        );
    }
}
