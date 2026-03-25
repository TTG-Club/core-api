package club.ttg.dnd5.domain.beastiary.rest.controller;

import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureDetailResponse;
import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureRequest;
import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureShortResponse;
import club.ttg.dnd5.domain.beastiary.service.CreatureFilterService;
import club.ttg.dnd5.domain.beastiary.service.CreatureService;

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

import java.util.List;

@Tag(name = "Бестиарий", description = "REST API для существ из бестиария")

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/bestiary")
public class CreatureController {
    private final CreatureService creatureService;
    private final CreatureFilterService creatureFilterService;

    @Operation(summary = "Проверить существо по URL", description = "Проверка существа по его уникальному URL.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Существо существует"),
            @ApiResponse(responseCode = "404", description = "Существо не существует")
    })
    @RequestMapping(path = "/{url}", method = RequestMethod.HEAD)
    public Boolean isSpellExist(@PathVariable String url) {
        return creatureService.existOrThrow(url);
    }



    @Operation(summary = "Поиск существ v2", description = "Поиск существ с Base64url-encoded фильтрами и пагинацией")
    @GetMapping("/search/v2")
    public List<CreatureShortResponse> searchV2(
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "f", required = false)
            @Schema(description = "Base64url-encoded JSON фильтров") String f,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size)
    {
        var request = club.ttg.dnd5.domain.filter.rest.SearchRequestResolver.resolve(
                f, search, page, size, club.ttg.dnd5.domain.beastiary.rest.dto.CreatureSearchRequest.class);
        return creatureService.searchV2(request);
    }

    @Operation(summary = "Получение детальной информации по URL", description = "Получение детальной информации по его уникальному URL.")
    @GetMapping("/{url}")
    public CreatureDetailResponse getByUrl(@PathVariable String url) {
        return creatureService.findDetailedByUrl(url);
    }

    @GetMapping("/{url}/raw")
    public CreatureRequest getFormByUrl(@PathVariable String url) {
        return creatureService.findFormByUrl(url);
    }



    @Operation(summary = "Получить метаданные фильтров v2", description = "Возвращает JSON для построения UI фильтров и использования в SearchRequest")
    @GetMapping("/filters/v2")
    public club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse getFiltersV2() {
        return creatureFilterService.getFilterMetadata();
    }

    @Operation(summary = "Добавление существа")
    @Secured("ADMIN")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String create(@RequestBody CreatureRequest request) {
        return creatureService.save(request);
    }

    @Operation(summary = "Предпросмотр существа")
    @Secured("ADMIN")
    @PostMapping("/preview")
    public CreatureDetailResponse preview(@RequestBody CreatureRequest request) {
        return creatureService.preview(request);
    }

    @Operation(summary = "Обновление существа")
    @Secured("ADMIN")
    @PutMapping("/{url}")
    public String update(@PathVariable String url,
                                      @Valid
                                      @RequestBody CreatureRequest request) {
        return creatureService.update(url, request);
    }

    @Operation(summary = "Сокрытие существа")
    @Secured("ADMIN")
    @DeleteMapping("/{url}")
    public String delete(@PathVariable String url) {
        return creatureService.delete(url);
    }
}
