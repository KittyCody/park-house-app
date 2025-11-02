package parkhouse.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "floors")
public class Floor {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int id;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT NOW()")
    private LocalDateTime createdAt;

    public Floor(int capacity, LocalDateTime createdAt) {
        this.capacity = capacity;
        this.createdAt = createdAt;
    }

    protected Floor() {}

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

