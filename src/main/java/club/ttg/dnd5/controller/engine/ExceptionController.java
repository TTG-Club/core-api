package club.ttg.dnd5.controller.engine;

import club.ttg.dnd5.dto.ResponseDto;
import club.ttg.dnd5.exception.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.io.IOException;

@Log4j2
@ControllerAdvice
@RequiredArgsConstructor
public class ExceptionController {

    @Value("${spring.servlet.multipart.max-file-size}")
    private String MAX_FILE_SIZE;

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ResponseDto> handleAuthenticationException(ApiException ex, HttpServletRequest request, HttpServletResponse response) {
        log.error(ExceptionUtils.getStackTrace(ex));

        return ResponseEntity.status(ex.getStatus())
                .body(new ResponseDto(
                        ex.getStatus().value(),
                        ex.getStatus().getReasonPhrase(),
                        ex.getMessage()));
    }

    @ExceptionHandler({SecurityException.class, AuthorizationDeniedException.class})
    public ResponseEntity<ResponseDto> handleSecurityException() {
        return convertToResponseEntity(HttpStatus.FORBIDDEN, "Доступ запрещен");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ResponseDto> handleRequestParamException(MissingServletRequestParameterException ex, HttpServletRequest request, HttpServletResponse response) {
        String message = String.format("Отсутствует необходимый параметр \"%s\"", ex.getParameterName());

        return convertToResponseEntity(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ResponseDto> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex, HttpServletRequest request, HttpServletResponse response) {
        String message = String.format("Максимальный размер загружаемых файлов – %s", MAX_FILE_SIZE);

        return convertToResponseEntity(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler({IOException.class, Exception.class})
    public ResponseEntity<ResponseDto> handleOtherExceptions(Exception ex, HttpServletRequest request, HttpServletResponse response) {
        log.error(ExceptionUtils.getStackTrace(ex));

        return convertToResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    private ResponseEntity<ResponseDto> convertToResponseEntity(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ResponseDto(status, message));
    }
}
