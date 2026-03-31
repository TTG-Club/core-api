package club.ttg.dnd5.domain.spell.rest.controller;

import org.springdoc.core.annotations.ParameterObject;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse;
import club.ttg.dnd5.domain.spell.rest.dto.SpellQueryRequest;
import club.ttg.dnd5.domain.spell.rest.dto.SpellDetailedResponse;
import club.ttg.dnd5.domain.spell.rest.dto.SpellShortResponse;
import club.ttg.dnd5.domain.spell.rest.dto.create.SpellRequest;
import club.ttg.dnd5.domain.spell.service.SpellFilterService;
import club.ttg.dnd5.domain.spell.service.SpellService;
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

    @Operation(summary = "Поиск заклинаний", description = "Поиск заклинаний с GET-параметрами фильтрации")
    @GetMapping("/search")
    public List<SpellShortResponse> search(@ParameterObject SpellQueryRequest request)
    {
        return spellService.search(request);
    }

    @GetMapping("/{url}")
    public SpellDetailedResponse getSpellByUrl(@PathVariable String url) {
        return spellService.findDetailedByUrl(url);
    }

    @GetMapping("/{url}/raw")
    public SpellRequest getSpellFormByUrl(@PathVariable String url) {
        return spellService.findFormByUrl(url);
    }



    @Operation(summary = "Получить метаданные фильтров", description = "Возвращает JSON для построения UI фильтров")
    @GetMapping("/filters")
    public FilterMetadataResponse getFilters(@RequestParam(required = false) Set<String> source) {
        return spellFilterService.getFilterMetadata(source != null ? source : Set.of());
    }

    @Secured("ADMIN")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String createSpell(@RequestBody SpellRequest request) {
        return spellService.save(request);
    }

    @Operation(summary = "Предпросмотр заклинания")
    @Secured("ADMIN")
    @PostMapping("/preview")
    public SpellDetailedResponse preview(@RequestBody SpellRequest request) {
        return spellService.preview(request);
    }

    @Secured("ADMIN")
    @PutMapping("/{url}")
    public String updateSpell(@PathVariable String url,
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
