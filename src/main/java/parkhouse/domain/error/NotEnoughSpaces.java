package parkhouse.domain.error;

public class NotEnoughSpaces extends RuntimeException {
    public NotEnoughSpaces() {
        super("Not enough spaces");
    }
}
