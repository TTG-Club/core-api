package club.ttg.dnd5.domain.beastiary.rest.controller;

import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureDetailResponse;
import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureRequest;
import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureShortResponse;
import club.ttg.dnd5.domain.beastiary.service.CreatureFilterService;
import club.ttg.dnd5.domain.beastiary.service.CreatureService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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

    @Operation(summary = "Поиск существ", description = "Поиск существа по именам")
    @PostMapping("/search")
    public List<CreatureShortResponse> search(@RequestParam(name = "query", required = false)
                                              @Valid
                                              @Size(min = 2)
                                              @Schema(description = "Строка поиска, если null-отдаются все сущности")
                                              String searchLine,
                                              @RequestBody(required = false) SearchBody searchBody) {
        return creatureService.search(searchLine, searchBody);
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

    @GetMapping("/filters")
    public FilterInfo getFilters() {
        return creatureFilterService.getDefaultFilterInfo();
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
        return creatureService.updateCreature(url, request);
    }

    @Operation(summary = "Сокрытие существа")
    @Secured("ADMIN")
    @DeleteMapping("/{url}")
    public String delete(@PathVariable String url) {
        return creatureService.deleteCreature(url);
    }
}
