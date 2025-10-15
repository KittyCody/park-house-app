package parkhouse.context;

import org.springframework.data.jpa.repository.JpaRepository;
import parkhouse.domain.Ticket;

import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {
}
