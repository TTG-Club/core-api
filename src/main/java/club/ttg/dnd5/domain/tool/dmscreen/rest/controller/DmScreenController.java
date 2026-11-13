package club.ttg.dnd5.domain.tool.dmscreen.rest.controller;

import club.ttg.dnd5.domain.tool.dmscreen.rest.dto.DmScreenRequest;
import club.ttg.dnd5.domain.tool.dmscreen.rest.dto.DmScreenResponse;
import club.ttg.dnd5.domain.tool.dmscreen.service.DmScreenService;
import club.ttg.dnd5.exception.ErrorResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Доступ: ширма есть только у авторизованного пользователя и только своя — она ищется по uuid
 * из JWT. {@code /api/v2/**} на уровне фильтров — permitAll, а {@code @Secured} ответил бы
 * анониму 403; клиент же по контракту ждёт 401, поэтому авторизацию проверяет сервис.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v2/tools/dm-screen")
@Tag(name = "Ширма Мастера",
        description = "REST API ширмы Мастера: окна с записями сайта и заметками, одна ширма на пользователя")
public class DmScreenController {

    private final DmScreenService screenService;

    @Operation(summary = "Ширма текущего пользователя; если её ещё не сохраняли — пустой список окон "
            + "и updatedAt = null. Без авторизации — 401")
    @GetMapping
    public DmScreenResponse findMine() {
        return screenService.findMine();
    }

    @Operation(summary = "Сохранение ширмы: окна заменяются целиком, первое сохранение создаёт запись. "
            + "Не больше 100 окон; нарушение правил окна — 400. Без авторизации — 401")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping
    public void saveMine(@RequestBody final DmScreenRequest request) {
        screenService.saveMine(request);
    }

    /** Тело не JSON или поле не того типа — 400, а не общий 500. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handleUnreadableBody() {
        return ResponseEntity.badRequest()
                .body(new ErrorResponseDto(HttpStatus.BAD_REQUEST, "Не удалось прочитать окна ширмы"));
    }
}
