package club.ttg.dnd5.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.io.Serial;

@Getter
public class ApiException extends RuntimeException {
	@Serial
    private static final long serialVersionUID = 1L;

    private final HttpStatus status;
    private final String message;

    public ApiException(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public ApiException(String message, HttpStatus status, String message1) {
        super(message);
        this.status = status;
        this.message = message1;
    }
}