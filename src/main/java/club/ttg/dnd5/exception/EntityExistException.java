package club.ttg.dnd5.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class EntityExistException extends RuntimeException {
    public EntityExistException(final String message) {
        super(message);
    }
}
