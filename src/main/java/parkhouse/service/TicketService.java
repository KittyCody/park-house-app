package parkhouse.service;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import parkhouse.context.FloorRepository;
import parkhouse.context.ParkingSettingsRepository;
import parkhouse.context.TicketRepository;
import parkhouse.domain.ParkingSettings;
import parkhouse.domain.Ticket;
import parkhouse.domain.error.InvalidOperationalHours;
import parkhouse.domain.error.NotEnoughSpaces;

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
    public Ticket createEntry(UUID entryGateId) {

        log.info("Ticket creation requested at gate {}", entryGateId);

        validateOperationalHours();

        long available = getAvailableSpaces();
        log.debug("Available spaces before ticket creation: {}", available);

        if (available < 1) {
            log.warn("Entry denied: no available spaces");
            throw new NotEnoughSpaces();
        }

        var now = LocalDateTime.now(clock);
        var ticket = new Ticket(entryGateId, now);

        Ticket saved = tickets.save(ticket);

        log.info("Ticket created successfully : id={}, hour={}", saved.getId(), now);

        return saved;
    }

    public Ticket getTicket(UUID id) {

        log.debug("Retrieving ticket {}", id);

        return tickets.findById(id)
                .orElseThrow(() -> {
                    log.error("Ticket not found : {}", id);
                    return new IllegalArgumentException("Ticket with id " + id + " not found");
                });
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

        var now = LocalDateTime.now(clock);

        long hours = Duration.between(ticket.getTimeOfEntry(), now).toHours();
        hours = Math.max(1, hours);

        return (int) hours * 2;
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

        if (ticket.getTimeOfPayment() != null) {
            throw new IllegalStateException("Ticket with id " + ticketId + " not paid");
        }

        if (ticket.getTimeOfExit() != null) {
            return;
        }

        ticket.setExitGateId(exitGateId);
        ticket.setTimeOfExit(LocalDateTime.now(clock));

        tickets.save(ticket);
    }
}
