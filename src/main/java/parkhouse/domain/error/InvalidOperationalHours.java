package parkhouse.domain.error;

public class InvalidOperationalHours extends RuntimeException {
    public InvalidOperationalHours() { super("Parking is closed at this time"); }
}



