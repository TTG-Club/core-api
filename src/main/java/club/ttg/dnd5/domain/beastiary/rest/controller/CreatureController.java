package club.ttg.dnd5.domain.beastiary.rest.controller;

import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureDetailResponse;
import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureQueryRequest;
import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureRequest;
import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureShortResponse;
import club.ttg.dnd5.domain.beastiary.service.CreatureFilterService;
import club.ttg.dnd5.domain.beastiary.service.CreatureService;
import club.ttg.dnd5.domain.beastiary.model.sense.CreatureSenses;
import club.ttg.dnd5.domain.common.dictionary.Alignment;
import club.ttg.dnd5.domain.common.dictionary.CreatureType;
import club.ttg.dnd5.domain.common.dictionary.Habitat;
import club.ttg.dnd5.domain.common.dictionary.Size;
import club.ttg.dnd5.domain.filter.rest.QueryParamFilterResolver;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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

    @Operation(summary = "Поиск существ", description = "Поиск существ с GET-параметрами фильтрации и пагинацией")
    @GetMapping("/search")
    public List<CreatureShortResponse> search(
            HttpServletRequest httpRequest,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size)
    {
        var params = httpRequest.getParameterMap();

        var request = new CreatureQueryRequest();
        request.setSearch(search);
        if (page != null) request.setPage(page);
        if (size != null) request.setPageSize(size);

        request.setCr(QueryParamFilterResolver.resolveLong(params, "cr"));
        request.setType(QueryParamFilterResolver.resolveEnum(params, "type", CreatureType.class));
        request.setSize(QueryParamFilterResolver.resolveEnum(params, "size", Size.class));
        request.setAlignment(QueryParamFilterResolver.resolveEnum(params, "alignment", Alignment.class));
        request.setHabitat(QueryParamFilterResolver.resolveEnum(params, "habitat", Habitat.class));
        request.setSenses(QueryParamFilterResolver.resolveEnum(params, "senses", CreatureSenses.class));
        request.setTraits(QueryParamFilterResolver.resolveString(params, "traits"));
        request.setTag(QueryParamFilterResolver.resolveString(params, "tag"));
        request.setLair(QueryParamFilterResolver.resolveSingleton(params, "lair"));
        request.setLegendaryAction(QueryParamFilterResolver.resolveSingleton(params, "legendaryAction"));
        request.setSource(QueryParamFilterResolver.resolveSources(params, "source"));

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
    public club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse getFilters() {
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
