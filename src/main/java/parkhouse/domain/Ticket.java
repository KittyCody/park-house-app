package parkhouse.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "floor_id", nullable = false)
    private Floor floor;

    @Column(nullable = false)
    private UUID entryGateId;

    private UUID exitGateId;

    @Column(nullable = false)
    private LocalDateTime timeOfEntry;

    private LocalDateTime timeOfExit;

    private LocalDateTime timeOfPayment;

    protected Ticket() {
    }

    public Ticket(UUID entryGateId, LocalDateTime now, Floor floor) {
        this.id = UUID.randomUUID();
        this.entryGateId = entryGateId;
        this.timeOfEntry = now;
        this.floor = floor;
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

    public void setTimeOfPayment(LocalDateTime timeOfPayment) {
        this.timeOfPayment = timeOfPayment;
    }

    public int getFloorId() {
        return floor.getId();
    }
}

