package parkhouse.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import parkhouse.context.FloorRepository;
import parkhouse.context.ParkingSettingsRepository;
import parkhouse.context.TicketRepository;
import parkhouse.domain.ParkingSettings;
import parkhouse.domain.Ticket;
import parkhouse.domain.error.InvalidOperationalHours;
import parkhouse.domain.error.NotEnoughSpaces;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TicketService {
    private final TicketRepository tickets;
    private final FloorRepository floors;
    private final ParkingSettingsRepository parkingSettings;
    private final Clock clock;

    public TicketService(TicketRepository tickets, FloorRepository floors, ParkingSettingsRepository parkingSettings, Clock clock) {
        this.tickets = tickets;
        this.floors = floors;
        this.parkingSettings = parkingSettings;
        this.clock = clock;
    }

    @Transactional
    public Ticket createEntry(UUID entryGateId) {

        validateOperationalHours();
        validateCapacity();

        var now = LocalDateTime.now(clock);
        var ticket = new Ticket(entryGateId, now);
        return tickets.save(ticket);
    }

    public Ticket getTicket(UUID id) {
        return tickets.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket with id " + id + " not found"));
    }

    private void validateOperationalHours() {
        var settings = parkingSettings.findTopByOrderByIdDesc()
                .orElse(ParkingSettings.createDefault());
        int hour = LocalDateTime.now(clock).getHour();

        if (hour < settings.openHour() || hour >= settings.closeHour()) {
            throw new InvalidOperationalHours();
        }
    }

    private void validateCapacity() {
        int vehicles = tickets.countAllByTimeOfExitIsNull();
        long available = floors.sumCapacity().orElse(0L);
        if (available == 0 || vehicles >= available) {
            throw new NotEnoughSpaces();
        }
    }
}
