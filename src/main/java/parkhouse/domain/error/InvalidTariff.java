package parkhouse.domain.error;

public class InvalidTariff extends RuntimeException {
    public InvalidTariff(String message) {
        super(message);
    }
}
