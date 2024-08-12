package club.ttg.dnd5.exceptions;

public class RefreshTokenException extends RuntimeException {
    public RefreshTokenException (String message){
        super(message);
    }
}
