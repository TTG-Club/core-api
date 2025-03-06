package club.ttg.dnd5.domain.background.rest.controller;

import club.ttg.dnd5.domain.background.rest.dto.BackgroundDetailResponse;
import club.ttg.dnd5.domain.background.rest.dto.BackgroundRequest;
import club.ttg.dnd5.domain.common.rest.dto.ShortResponse;
import club.ttg.dnd5.exception.EntityNotFoundException;
import club.ttg.dnd5.domain.background.service.BackgroundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v2/background")
@Tag(name = "Предыстории", description = "REST API предысторий персонажа")
public class BackgroundController {
    private final BackgroundService backgroundService;

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "URL предыстории существует"),
            @ApiResponse(responseCode = "404", description = "URL предыстория не существует"),
    })
    @RequestMapping(path = "/{backgroundUrl}", method = RequestMethod.HEAD)
    public boolean existByUrl(@PathVariable final String backgroundUrl) {
        var exist = backgroundService.exists(backgroundUrl);
        if (!exist) {
            throw new EntityNotFoundException("URL предыстории не найден");
        }
        return true;
    }

    @Operation(summary = "Детальная информация о предыстории", description = "Возвращает объект с детальной информацией о предыстории")
    @GetMapping("{backgroundUrl}")
    public BackgroundDetailResponse findBackground(@PathVariable final String backgroundUrl) {
        return backgroundService.getBackground(backgroundUrl);
    }

    @Operation(summary = "Краткой информации о предысториях", description = "Возвращает коллекцию с предысториями в кратком виде")
    @PostMapping("/search")
    public Collection<ShortResponse> findBackgrounds() {
        return backgroundService.getBackgrounds();
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Предыстория успешно создана"),
            @ApiResponse(responseCode = "404", description = "Предыстория не найден"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    @Secured("ADMIN")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public BackgroundDetailResponse addBackgrounds(@RequestBody final BackgroundRequest backgroundDto) {
        return backgroundService.addBackground(backgroundDto);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Предыстория успешно обновлена"),
            @ApiResponse(responseCode = "404", description = "Предыстория не найден"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    @Secured("ADMIN")
    @PutMapping("{backgroundUrl}")
    public BackgroundDetailResponse updateBackgrounds(
            @PathVariable final String backgroundUrl,
            @RequestBody final BackgroundRequest backgroundDto) {
        return backgroundService.updateBackgrounds(backgroundUrl, backgroundDto);
    }

    @Operation(summary = "Помечает предысторию как скрытую для списков")
    @Secured("ADMIN")
    @DeleteMapping("{backgroundUrl}")
    public ShortResponse deleteBackgrounds(
            @PathVariable final String backgroundUrl) {
        return backgroundService.deleteBackgrounds(backgroundUrl);
    }
}
