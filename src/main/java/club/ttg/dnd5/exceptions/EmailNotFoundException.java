package club.ttg.dnd5.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "We could not find an account associated with this email address. Please check your email address or sign up for a new account.")
public class EmailNotFoundException extends RuntimeException {

    /**
     * Constructs a new EmailNotFoundException with the specified error message.
     *
     * @param message the error message describing the exception
     */
    public EmailNotFoundException(String message) {
        super(message);
    }
}