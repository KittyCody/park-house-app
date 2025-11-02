package parkhouse.domain.error;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotEnoughSpaces.class)
    public ResponseEntity<ApiError> handleNotEnoughSpaces(NotEnoughSpaces ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError("NOT_ENOUGH_SPACES", ex.getMessage()));
    }

    @ExceptionHandler(InvalidOperationalHours.class)
    public ResponseEntity<ApiError> handleClosed(InvalidOperationalHours ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiError("OUTSIDE_OF_OPERATIONAL_HOURS", ex.getMessage()));
    }
    record ApiError(String code, String message) {}
}
