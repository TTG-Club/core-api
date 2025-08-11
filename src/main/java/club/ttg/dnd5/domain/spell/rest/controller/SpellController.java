package club.ttg.dnd5.domain.spell.rest.controller;

import club.ttg.dnd5.domain.common.rest.dto.PageResponse;
import club.ttg.dnd5.domain.filter.model.FilterInfo;
import club.ttg.dnd5.domain.filter.model.SearchBody;
import club.ttg.dnd5.domain.spell.rest.dto.SpellDetailedResponse;
import club.ttg.dnd5.domain.spell.rest.dto.SpellShortResponse;
import club.ttg.dnd5.domain.spell.rest.dto.create.SpellRequest;
import club.ttg.dnd5.domain.spell.service.SpellFilterService;
import club.ttg.dnd5.domain.spell.service.SpellService;
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

@Tag(name = "Заклинания", description = "REST API заклинаний")

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/spells")
public class SpellController {
    private final SpellService spellService;
    private final SpellFilterService spellFilterService;

    @Operation(summary = "Проверить заклинание по URL", description = "Проверка заклинание по его уникальному URL.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Заклинание существует"),
            @ApiResponse(responseCode = "404", description = "Заклинание не существует")
    })
    @RequestMapping(path = "/{url}", method = RequestMethod.HEAD)
    public Boolean isSpellExist(@PathVariable String url) {
        return spellService.existOrThrow(url);
    }

    @Operation(summary = "Поиск заклинаний", description = "Поиск заклинания по именам")
    @PostMapping("/search")
    public PageResponse<SpellShortResponse> getSpells(@RequestParam(name = "query", required = false)
                                              @Valid
                                              @Size(min = 3)
                                              @Schema(description = "Строка поиска, если null-отдаются все сущности")
                                              String searchLine,
                                                      @RequestParam(required = false, defaultValue = "1")
                                              int page,
                                                      @RequestParam(required = false, defaultValue = "120")
                                              int limit,
                                                      @RequestParam(required = false, defaultValue = "name")
                                              String sort,
                                                      @RequestBody(required = false) SearchBody searchBody) {
        return spellService.search(searchLine, page, limit, sort, searchBody);
    }

    @GetMapping("/{url}")
    public SpellDetailedResponse getSpellByUrl(@PathVariable String url) {
        return spellService.findDetailedByUrl(url);
    }

    @GetMapping("/{url}/raw")
    public SpellRequest getSpellFormByUrl(@PathVariable String url) {
        return spellService.findFormByUrl(url);
    }

    @GetMapping("/filters")
    public FilterInfo getFilters() {
        return spellFilterService.getDefaultFilterInfo();
    }

    @Secured("ADMIN")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SpellDetailedResponse createSpell(@RequestBody SpellRequest request) {
        return spellService.save(request);
    }

    @Secured("ADMIN")
    @PutMapping("/{url}")
    public SpellDetailedResponse updateSpell(@PathVariable String url,
                                             @Valid
                                             @RequestBody SpellRequest request) {
        return spellService.update(url, request);
    }

    @Secured("ADMIN")
    @DeleteMapping("/{url}")
    public void deleteSpell(@PathVariable String url) {
        spellService.delete(url);
    }

}
