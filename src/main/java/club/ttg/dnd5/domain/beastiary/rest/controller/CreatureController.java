package club.ttg.dnd5.domain.beastiary.rest.controller;

import org.springdoc.core.annotations.ParameterObject;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse;
import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureDetailResponse;
import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureQueryRequest;
import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureRequest;
import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureShortResponse;
import club.ttg.dnd5.domain.beastiary.service.CreatureFilterService;
import club.ttg.dnd5.domain.beastiary.service.CreatureService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

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

    @Operation(summary = "Поиск существ", description = "Поиск существ с GET-параметрами фильтрации и пагинацией")
    @GetMapping("/search")
    public List<CreatureShortResponse> search(@ParameterObject CreatureQueryRequest request)
    {
        return creatureService.search(request);
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

    @Operation(summary = "Получить метаданные фильтров", description = "Возвращает JSON для построения UI фильтров")
    @GetMapping("/filters")
    public FilterMetadataResponse getFilters(@RequestParam(required = false) Set<String> source) {
        return creatureFilterService.getFilterMetadata(source != null ? source : Set.of());
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
