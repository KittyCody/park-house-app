package parkhouse.domain;

import jakarta.annotation.Nullable;
import parkhouse.domain.error.InvalidOperationalHours;
import jakarta.persistence.*;

@Entity
@Table(name = "parking_settings")
public class ParkingSettings {

    private static final int MinOperationalHours = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int id;

    private int openHour;
    private int closeHour;

    protected ParkingSettings() {
    }

    public ParkingSettings(int openHour, int closeHour) {

        if (!isValidOperationalHours(openHour, closeHour)) {
            throw new InvalidOperationalHours();
        }

        this.openHour = openHour;
        this.closeHour = closeHour;
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
        return new ParkingSettings(8, 22);
    }

    @Nullable
    public InvalidOperationalHours updateOperationalHours(int openHour, int closeHour) {
        if (!isValidOperationalHours(openHour, closeHour)) {
            return new InvalidOperationalHours();
        }

        this.openHour = openHour;
        this.closeHour = closeHour;

        return null;
    }

    public int openHour() {
        return openHour;
    }

    public int closeHour() {
        return closeHour;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
