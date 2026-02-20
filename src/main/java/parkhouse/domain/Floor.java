package parkhouse.domain;

import jakarta.persistence.*;

import java.time.Clock;
import java.time.LocalDateTime;

@Entity
@Table(name = "floors")
public class Floor {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int id;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Floor() {
    }

    public Floor(int capacity) {
        this.capacity = capacity;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now(Clock.systemUTC());
    }

    public int getId() {
        return id;
    }

    public int getCapacity() {
        return capacity;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
