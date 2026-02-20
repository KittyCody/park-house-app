package parkhouse.domain;

import jakarta.persistence.*;
import parkhouse.domain.error.InvalidOperationalHours;

@Entity
@Table(name = "parking_settings")
public class ParkingSettings {

    private static final int MinOperationalHours = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int id;

    @Column(nullable = false)
    private int openHour;

    @Column(nullable = false)
    private int closeHour;

    @Column(nullable = false)
    private int pricePerHour;

    protected ParkingSettings() {
    }

    public ParkingSettings(int openHour, int closeHour, int pricePerHour) {

        if (!isValidOperationalHours(openHour, closeHour)) {
            throw new InvalidOperationalHours();
        }

        this.openHour = openHour;
        this.closeHour = closeHour;
        this.pricePerHour = pricePerHour;
    }

    public static boolean isValidOperationalHours(int openHour, int closeHour) {
        return is24HourFormat(openHour)
                && is24HourFormat(closeHour)
                && closeHour > openHour
                && closeHour - openHour > MinOperationalHours;
    }

    private static boolean is24HourFormat(int hour) {
        return hour >= 0 && hour <= 24;
    }

    public static ParkingSettings createDefault() {
        return new ParkingSettings(8, 22, 3);
    }

    public void updateOperationalHours(int openHour, int closeHour) {
        if (!isValidOperationalHours(openHour, closeHour)) {
            throw new InvalidOperationalHours();
        }

        this.openHour = openHour;
        this.closeHour = closeHour;
    }

    public void updatePrice(int pricePerHour) {
        this.pricePerHour = pricePerHour;
    }

    public int openHour() {
        return openHour;
    }

    public int closeHour() {
        return closeHour;
    }

    public int pricePerHour() {
        return pricePerHour;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
