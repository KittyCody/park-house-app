package parkhouse.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import parkhouse.context.TicketRepository;
import parkhouse.domain.Ticket;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TicketService {
    private final TicketRepository tickets;
    private final Clock clock;

    public TicketService(TicketRepository tickets, Clock clock) {
        this.tickets = tickets;
        this.clock = clock;
    }

    @Transactional
    public Ticket createEntry(UUID entryGateId) {
        var now = LocalDateTime.now(clock);
        var ticket = new Ticket(entryGateId, now);
        return tickets.save(ticket);
    }

    public Ticket get(UUID id) {
        return tickets.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket with id " + id + " not found"));
    }
}
