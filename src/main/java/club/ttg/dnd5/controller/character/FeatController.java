package club.ttg.dnd5.controller.character;

import club.ttg.dnd5.dto.character.FeatDto;
import club.ttg.dnd5.service.character.FeatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/feat")
@Tag(name = "Черты ", description = "REST API черт персонажа")
public class FeatController {
    private final FeatService featService;

    /**
     * Проверка существования черта по URL.
     *
     * @param url URL черты.
     * @return 204, если черта с таким URL не существует; 409, если существует.
     */
    @Operation(
            summary = "Проверка существования черты",
            description = "Возвращает 204 (No Content), если черта с указанным URL не существует, или 409 (Conflict), если существует."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Черта с указанным URL не найден."),
            @ApiResponse(responseCode = "409", description = "Черта с указанным URL уже существует.")
    })
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(path = "/{url}", method = RequestMethod.HEAD)
    public ResponseEntity<Boolean> existByUrl(@PathVariable final String url) {
        boolean exists = featService.existByUrl(url);
        if (exists) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } else {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
    }

    @Operation(summary = "Получение детального описания черты")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Черта успешно получена"),
            @ApiResponse(responseCode = "404", description = "Черта не найдена")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{url}")
    public FeatDto getFeat(@PathVariable final String url) {
        return featService.getFeat(url);
    }

    @Operation(summary = "Получение списка краткого описания черты")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Черты успешно получена")
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/search")
    public Collection<FeatDto> getFeats() {
        return featService.getFeats();
    }

    @Operation(summary = "Добавление черты")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Черта успешно добавлена"),
            @ApiResponse(responseCode = "400", description = "Черта уже существует"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public FeatDto addFeats(@RequestBody final FeatDto featDto) {
        return featService.addFeat(featDto);
    }

    @Operation(summary = "Обновление черты")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Черта успешно обновлена"),
            @ApiResponse(responseCode = "200", description = "Черта не найдена"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("{featUrl}")
    public FeatDto updateFeats(@PathVariable final String featUrl,
                               @RequestBody final FeatDto featDto) {
        return featService.updateFeat(featUrl, featDto);
    }

    @Operation(summary = "Скрывает черту")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Черта удалена из общего списка"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("{featUrl}")
    public FeatDto deleteFeats(@PathVariable final String featUrl) {
        return featService.delete(featUrl);
    }
}
