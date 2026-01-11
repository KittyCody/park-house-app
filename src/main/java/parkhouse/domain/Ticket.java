package parkhouse.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID entryGateId;

    private UUID exitGateId;

    @Column(nullable = false)
    private LocalDateTime timeOfEntry;

    private LocalDateTime timeOfExit;

    private LocalDateTime timeOfPayment;

    protected Ticket() {
    }

    public Ticket(UUID entryGateId, LocalDateTime now) {
        this.id = UUID.randomUUID();
        this.entryGateId = entryGateId;
        this.timeOfEntry = now;
    }

    public UUID getId() {
        return id;
    }

    public UUID getEntryGateId() {
        return entryGateId;
    }

    public UUID getExitGateId() {
        return exitGateId;
    }

    public void setExitGateId(UUID exitGateId) {
        this.exitGateId = exitGateId;
    }

    public LocalDateTime getTimeOfEntry() {
        return timeOfEntry;
    }

    public LocalDateTime getTimeOfExit() {
        return timeOfExit;
    }

    public void setTimeOfExit(LocalDateTime timeOfExit) {
        this.timeOfExit = timeOfExit;
    }

    public LocalDateTime getTimeOfPayment() {
        return timeOfPayment;
    }

    public void setTimeOfPayment(LocalDateTime timeOfpayment) {
        this.timeOfPayment = timeOfpayment;
    }
}

