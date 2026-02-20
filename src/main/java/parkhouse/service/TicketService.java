package parkhouse.service;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import parkhouse.context.FloorRepository;
import parkhouse.context.ParkingSettingsRepository;
import parkhouse.context.TicketRepository;
import parkhouse.domain.Floor;
import parkhouse.domain.ParkingSettings;
import parkhouse.domain.Ticket;
import parkhouse.domain.error.InvalidOperationalHours;
import parkhouse.domain.error.NotEnoughSpaces;
import parkhouse.dto.TicketStatusResponse;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketService.class);

    private final TicketRepository tickets;
    private final FloorRepository floors;
    private final ParkingSettingsRepository parkingSettings;
    private final Clock clock;

    public TicketService(TicketRepository tickets,
                         FloorRepository floors,
                         ParkingSettingsRepository parkingSettings,
                         Clock clock) {
        this.tickets = tickets;
        this.floors = floors;
        this.parkingSettings = parkingSettings;
        this.clock = clock;
    }

    @Transactional
    public Ticket createEntry(UUID entryGateId, int floorId) {

        log.info("Ticket creation requested at gate {}, floor {}", entryGateId, floorId);

        validateOperationalHours();

        long available = getAvailableSpaces();
        if (available < 1) throw new NotEnoughSpaces();

        Floor floor = floors.findById(floorId)
                .orElseThrow(() -> new IllegalArgumentException("Floor " + floorId + " not found"));

        var now = LocalDateTime.now(clock);
        var ticket = new Ticket(entryGateId, now, floor);

        return tickets.save(ticket);
    }

    private void validateOperationalHours() {

        var settings = parkingSettings.findTopByOrderByIdDesc()
                .orElseGet(() -> {
                    log.warn("No configuration found → using default values");
                    return ParkingSettings.createDefault();
                });

        int hour = LocalDateTime.now(clock).getHour();

        log.debug("Current hour : {}, open : {}, close : {}",
                hour, settings.openHour(), settings.closeHour());

        if (hour < settings.openHour() || hour >= settings.closeHour()) {
            log.error("Access denied : parking closed (current hour : {})", hour);
            throw new InvalidOperationalHours();
        }
    }

    public long getAvailableSpaces() {

        int parkedVehicles = tickets.countAllByTimeOfExitIsNull();
        long capacity = floors.sumCapacity().orElse(0L);

        long available = Math.max(0, capacity - parkedVehicles);

        log.debug("Capacity : {}, parked vehicles : {}, Available : {}",
                capacity, parkedVehicles, available);

        return available;
    }

    public int calculatePrice(UUID ticketId) {

        var ticket = tickets.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket with id " + ticketId + " not found"));

        var settings = parkingSettings.findTopByOrderByIdDesc()
                .orElseGet(() -> {
                    log.warn("No configuration found → using default values");
                    return ParkingSettings.createDefault();
                });

        var now = LocalDateTime.now(clock);

        long hours = Duration.between(ticket.getTimeOfEntry(), now).toHours();
        hours = Math.max(1, hours);

        return (int) hours * settings.pricePerHour();
    }

    @Transactional
    public void pay(UUID ticketId) {

        var ticket = tickets.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket with id " + ticketId + " not found"));

        if (ticket.getTimeOfPayment() != null) {
            return;  //already paid
        }

        ticket.setTimeOfPayment(LocalDateTime.now(clock));
        tickets.save(ticket);
    }

    @Transactional
    public void exit(UUID ticketId, UUID exitGateId) {

        var ticket = tickets.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket with id " + ticketId + " not found"));

        if (ticket.getTimeOfPayment() == null) {
            throw new IllegalStateException("Ticket with id " + ticketId + " not paid");
        }

        if (ticket.getTimeOfExit() != null) {
            throw new IllegalStateException("Ticket with id " + ticketId + " already exited");
        }

        ticket.setExitGateId(exitGateId);
        ticket.setTimeOfExit(LocalDateTime.now(clock));

        tickets.save(ticket);
    }

    public TicketStatusResponse getStatus(UUID ticketId) {

        var ticketOpt = tickets.findById(ticketId);

        if (ticketOpt.isEmpty()) {
            return TicketStatusResponse.invalid("TICKET_NOT_FOUND");
        }

        var ticket = ticketOpt.get();

        if (ticket.getTimeOfExit() != null) {
            return TicketStatusResponse.invalid("TICKET_ALREADY_EXITED");
        }

        var now = LocalDateTime.now(clock);

        int durationMinutes = (int) Duration.between(ticket.getTimeOfEntry(), now).toMinutes();
        durationMinutes = Math.max(0, durationMinutes);

        int amountCents = calculatePrice(ticketId);

        if (ticket.getTimeOfPayment() != null) {
            return TicketStatusResponse.paid(ticket.getTimeOfEntry(), durationMinutes, amountCents);
        }

        return TicketStatusResponse.paymentRequired(ticket.getTimeOfEntry(), durationMinutes, amountCents);
    }

}
