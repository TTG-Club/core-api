package club.ttg.dnd5.domain.background.rest.controller;

import club.ttg.dnd5.domain.background.rest.dto.BackgroundDetailResponse;
import club.ttg.dnd5.domain.background.rest.dto.BackgroundRequest;
import club.ttg.dnd5.domain.background.rest.dto.BackgroundShortResponse;
import club.ttg.dnd5.domain.background.service.BackgroundFilterService;
import club.ttg.dnd5.domain.common.rest.dto.PageResponse;
import club.ttg.dnd5.domain.filter.model.FilterInfo;
import club.ttg.dnd5.domain.filter.model.SearchBody;
import club.ttg.dnd5.exception.EntityNotFoundException;
import club.ttg.dnd5.domain.background.service.BackgroundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v2/backgrounds")
@Tag(name = "Предыстории", description = "REST API предысторий персонажа")
public class BackgroundController {
    private final BackgroundFilterService backgroundFilterService;
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
    @GetMapping("{url}")
    public BackgroundDetailResponse findBackground(@PathVariable final String url) {
        return backgroundService.getBackground(url);
    }

    @GetMapping("/{url}/raw")
    public BackgroundRequest getSpellFormByUrl(@PathVariable String url) {
        return backgroundService.findFormByUrl(url);
    }

    @Operation(summary = "Краткой информации о предысториях", description = "Возвращает коллекцию с предысториями в кратком виде")
    @PostMapping("/search")
    public PageResponse<BackgroundShortResponse> findBackgrounds(
        @RequestParam(name = "query", required = false)
        @Valid
        @Size(min = 2)
        @Schema( description = "Строка поиска, если null-отдаются все сущности")
        String searchLine,
        @RequestParam(required = false, defaultValue = "1")
        int page,
        @RequestParam(required = false, defaultValue = "120")
        int limit,
        @RequestParam(required = false, defaultValue = "name")
        String[] sort,
        @RequestBody(required = false) SearchBody searchBody) {
        return backgroundService.getBackgrounds(searchLine, page, limit, sort, searchBody);
    }

    @GetMapping("/filters")
    public FilterInfo getFilters() {
        return backgroundFilterService.getDefaultFilterInfo();
    }

    @Operation(summary = "Создание предыстории", description = "Возвращает ссылку на созданную предысторию")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Предыстория успешно создана"),
            @ApiResponse(responseCode = "404", description = "Предыстория не найден"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    @Secured("ADMIN")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public String addBackgrounds(@RequestBody final BackgroundRequest backgroundDto) {
        return backgroundService.addBackground(backgroundDto);
    }

    @Operation(summary = "Предпросмотр предыстории")
    @Secured("ADMIN")
    @PostMapping("/preview")
    public BackgroundDetailResponse preview(@RequestBody BackgroundRequest request) {
        return backgroundService.preview(request);
    }

    @Operation(summary = "Обновление предыстории", description = "Возвращает ссылку на обновленную предысторию")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Предыстория успешно обновлена"),
            @ApiResponse(responseCode = "404", description = "Предыстория не найден"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    @Secured("ADMIN")
    @PutMapping("{url}")
    public String updateBackgrounds(
            @PathVariable final String url,
            @RequestBody final BackgroundRequest request) {
        return backgroundService.updateBackgrounds(url, request);
    }

    @Operation(summary = "Помечает предысторию как скрытую для списков")
    @Secured("ADMIN")
    @DeleteMapping("{url}")
    public String deleteBackgrounds(
            @PathVariable final String url) {
        return backgroundService.deleteBackgrounds(url);
    }
}
