package club.ttg.dnd5.domain.spell.rest.controller;

import club.ttg.dnd5.domain.filter.rest.SearchRequestResolver;
import club.ttg.dnd5.domain.spell.rest.dto.SpellDetailedResponse;
import club.ttg.dnd5.domain.spell.rest.dto.SpellSearchRequest;
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

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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



    @Operation(summary = "Поиск заклинаний v2", description = "Поиск заклинаний с Base64url-encoded фильтрами и пагинацией")
    @GetMapping("/search/v2")
    public List<SpellShortResponse> searchV2(
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "f", required = false)
            @Schema(description = "Base64url-encoded JSON фильтров") String f,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size)
    {
        SpellSearchRequest request = SearchRequestResolver.resolve(f, search, page, size, SpellSearchRequest.class);
        return spellService.searchV2(request);
    }

    @GetMapping("/{url}")
    public SpellDetailedResponse getSpellByUrl(@PathVariable String url) {
        return spellService.findDetailedByUrl(url);
    }

    @GetMapping("/{url}/raw")
    public SpellRequest getSpellFormByUrl(@PathVariable String url) {
        return spellService.findFormByUrl(url);
    }



    @Operation(summary = "Получить метаданные фильтров v2", description = "Возвращает JSON для построения UI фильтров и использования в SearchRequest")
    @GetMapping("/filters/v2")
    public club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse getFiltersV2() {
        return spellFilterService.getFilterMetadata();
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
