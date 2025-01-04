package club.ttg.dnd5.controller;

import club.ttg.dnd5.dto.ResponseDto;
import club.ttg.dnd5.exception.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Log4j2
@ControllerAdvice
public class ExceptionController {
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ResponseDto> handleAuthenticationException(ApiException ex, HttpServletRequest request, HttpServletResponse response) {
        log.error(ExceptionUtils.getStackTrace(ex));

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ResponseDto(
                        ex.getStatus().value(),
                        ex.getStatus().getReasonPhrase(),
                        ex.getMessage()));
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<Object> handleOtherExceptions(Exception ex, HttpServletRequest request, HttpServletResponse response) {
        log.error(ExceptionUtils.getStackTrace(ex));

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDto(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                        ex.getMessage()));
    }
}
