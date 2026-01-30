package parkhouse.dto;

import java.time.LocalDateTime;

public record TicketStatusResponse(
        String status,
        LocalDateTime entryTime,
        Integer durationMinutes,
        Integer amountCents,
        String reason

) {

    public static TicketStatusResponse invalid(String reason) {
        return new TicketStatusResponse("INVALID", null, null, null, reason);
    }

    public static TicketStatusResponse paymentRequired(LocalDateTime entryTime, int durationMinutes, int amountCents) {
        return new TicketStatusResponse("PAYMENT_REQUIRED", entryTime, durationMinutes, amountCents, null);
    }

    public static TicketStatusResponse paid(LocalDateTime entryTime, int durationMinutes, int amountCents) {
        return new TicketStatusResponse("PAID", entryTime, durationMinutes, amountCents, null);
    }
}
