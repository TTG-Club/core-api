package club.ttg.dnd5.domain.background.rest.controller;

import club.ttg.dnd5.domain.background.rest.dto.BackgroundDetailResponse;
import club.ttg.dnd5.domain.background.rest.dto.BackgroundRequest;
import club.ttg.dnd5.domain.background.rest.dto.BackgroundSelectResponse;
import club.ttg.dnd5.domain.background.rest.dto.BackgroundShortResponse;
import club.ttg.dnd5.domain.background.service.BackgroundFilterService;
import club.ttg.dnd5.domain.background.service.BackgroundService;
import club.ttg.dnd5.exception.EntityNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

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
    public BackgroundRequest getBackgroundFormByUrl(@PathVariable String url) {
        return backgroundService.findFormByUrl(url);
    }

    @GetMapping("/select")
    public Collection<BackgroundSelectResponse> getBackgroundSelect(
            @RequestParam(name = "query", required = false)
            @Valid
            @Schema( description = "Строка поиска, если null-отдаются все сущности")
            String searchLine) {
        return backgroundService.getBackgroundsSelect(searchLine);
    }



    @Operation(summary = "Поиск предысторий v2", description = "Поиск предысторий с Base64url-encoded фильтрами и пагинацией")
    @GetMapping("/search/v2")
    public Collection<BackgroundShortResponse> searchV2(
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "f", required = false)
            @Schema(description = "Base64url-encoded JSON фильтров") String f,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size)
    {
        var request = club.ttg.dnd5.domain.filter.rest.SearchRequestResolver.resolve(
                f, search, page, size, club.ttg.dnd5.domain.background.rest.dto.BackgroundSearchRequest.class);
        return backgroundService.searchV2(request);
    }



    @Operation(summary = "Получить метаданные фильтров v2")
    @GetMapping("/filters/v2")
    public club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse getFiltersV2() {
        return backgroundFilterService.getFilterMetadata();
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
