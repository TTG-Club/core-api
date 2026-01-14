package club.ttg.dnd5.domain.feat.rest.controller;

import club.ttg.dnd5.domain.feat.model.FeatCategory;
import club.ttg.dnd5.domain.feat.rest.dto.FeatDetailResponse;
import club.ttg.dnd5.domain.feat.rest.dto.FeatRequest;

import club.ttg.dnd5.domain.feat.rest.dto.FeatSelectResponse;
import club.ttg.dnd5.domain.feat.rest.dto.FeatShortResponse;
import club.ttg.dnd5.domain.feat.service.FeatFilterService;
import club.ttg.dnd5.domain.feat.service.FeatService;
import club.ttg.dnd5.domain.filter.model.FilterInfo;
import club.ttg.dnd5.domain.filter.model.SearchBody;
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

import java.util.Collection;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/feats")
@Tag(name = "Черты ", description = "REST API черт персонажа")
public class FeatController {
    private final FeatService featService;
    private final FeatFilterService featFilterService;

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(path = "/{url}", method = RequestMethod.HEAD)
    public boolean existByUrl(@PathVariable final String url) {
        return featService.existOrThrow(url);
    }

    @Operation(summary = "Получение детального описания черты")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Черта успешно получена"),
            @ApiResponse(responseCode = "404", description = "Черта не найдена")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{url}")
    public FeatDetailResponse getFeat(@PathVariable final String url) {
        return featService.getFeat(url);
    }

    @GetMapping("/{url}/raw")
    public FeatRequest getFeatFormByUrl(@PathVariable String url) {
        return featService.findFormByUrl(url);
    }

    @Operation(summary = "Список черт", description = "Список черт, поиск и фильтрация")
    @GetMapping
    public Collection<FeatShortResponse> getFeats(
                                              @RequestParam(name = "search", required = false)
                                              @Valid
                                              @Size(min = 2)
                                              @Schema(description = "Строка поиска, если null-отдаются все сущности")
                                              String searchLine,
                                              @Schema(description = "упакованный в строку json фильтров")
                                              @RequestParam(required = false) String filter) {
        return featService.search(searchLine, filter);
    }

    @Operation(summary = "Получение списка черт")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Черты успешно получена")
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/search")
    public Collection<FeatShortResponse> getFeats(@RequestParam(name = "query", required = false)
                                                  @Valid
                                                  @Size(min = 2)
                                                  @Schema( description = "Строка поиска, если null-отдаются все сущности")
                                                  String searchLine,
                                                  @RequestBody(required = false) SearchBody searchBody
    ) {
        return featService.getFeats(searchLine, searchBody);
    }

    @Operation(summary = "Получение списка черт для селекта")
    @GetMapping("/select")
    public Collection<FeatSelectResponse> getFeatSelect(
            @RequestParam(name = "query", required = false)
            @Valid
            @Size(min = 2)
            @Schema( description = "Строка поиска, если null-отдаются все сущности")
            String searchLine,
            @Schema( description = "Категория, если null-отдаются все черты")
            @RequestParam(required = false) Set<FeatCategory> categories) {
        return featService.getFeatsSelect(searchLine, categories);
    }

    @GetMapping("/filters")
    public FilterInfo getFilters() {
        return featFilterService.getDefaultFilterInfo();
    }

    @Secured("ADMIN")
    @Operation(summary = "Добавление черты")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Черта успешно добавлена"),
            @ApiResponse(responseCode = "400", description = "Черта уже существует"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public String addFeats(@RequestBody final FeatRequest featDto) {
        return featService.addFeat(featDto);
    }

    @Operation(summary = "Предпросмотр черты")
    @Secured("ADMIN")
    @PostMapping("/preview")
    public FeatDetailResponse preview(@RequestBody FeatRequest request) {
        return featService.preview(request);
    }

    @Secured("ADMIN")
    @Operation(summary = "Обновление черты")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Черта успешно обновлена"),
            @ApiResponse(responseCode = "200", description = "Черта не найдена"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    @ResponseStatus(HttpStatus.OK)
    @PutMapping("{url}")
    public String updateFeats(@PathVariable final String url,
                                          @RequestBody final FeatRequest featDto) {
        return featService.updateFeat(url, featDto);
    }

    @Secured("ADMIN")
    @Operation(summary = "Скрывает черту")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Черта удалена из общего списка"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("{url}")
    public String deleteFeats(@PathVariable final String url) {
        return featService.delete(url);
    }
}
