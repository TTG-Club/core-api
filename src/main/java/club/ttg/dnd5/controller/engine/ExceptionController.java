package club.ttg.dnd5.controller.engine;

import club.ttg.dnd5.dto.ErrorResponseDto;
import club.ttg.dnd5.exception.ApiException;
import club.ttg.dnd5.exception.ContentNotFoundException;
import club.ttg.dnd5.exception.EntityExistException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.io.IOException;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class ExceptionController {
    @Value("${spring.servlet.multipart.max-file-size}")
    private String MAX_FILE_SIZE;

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponseDto> handleApiException(ApiException ex) {
        log.error(ExceptionUtils.getStackTrace(ex));
        return convertToResponseEntity(ex.getStatus(), ex.getMessage());
    }

    @ExceptionHandler({SecurityException.class, AuthorizationDeniedException.class})
    public ResponseEntity<ErrorResponseDto> handleSecurityException() {
        return convertToResponseEntity(HttpStatus.FORBIDDEN, "Доступ запрещен");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponseDto> handleRequestParamException(MissingServletRequestParameterException exception) {
        String message = String.format("Отсутствует необходимый параметр \"%s\"", exception.getParameterName());

        return convertToResponseEntity(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponseDto> handleMaxUploadSizeExceededException() {
        String message = String.format("Максимальный размер загружаемых файлов – %s", MAX_FILE_SIZE);

        return convertToResponseEntity(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler({NoHandlerFoundException.class, IOException.class, Exception.class})
    public ResponseEntity<ErrorResponseDto> handleOtherExceptions(Exception exception) {
        log.error(ExceptionUtils.getStackTrace(exception));

        return convertToResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleEntityNotFound(Exception exception) {
        return convertToResponseEntity(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(EntityExistException.class)
    public ResponseEntity<ErrorResponseDto> handleHandleEntityExistException(Exception exception) {
        return convertToResponseEntity(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(ContentNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleContentNotFoundException(Exception exception) {
        return convertToResponseEntity(HttpStatus.NO_CONTENT, exception.getMessage());
    }

    private ResponseEntity<ErrorResponseDto> convertToResponseEntity(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ErrorResponseDto(status, message));
    }
}
