package club.ttg.dnd5.domain.spell.rest.controller;

import org.springdoc.core.annotations.ParameterObject;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse;
import club.ttg.dnd5.domain.spell.rest.dto.SpellQueryRequest;
import club.ttg.dnd5.domain.spell.rest.dto.SpellDetailedResponse;
import club.ttg.dnd5.domain.spell.rest.dto.SpellShortResponse;
import club.ttg.dnd5.domain.spell.rest.dto.create.SpellRequest;
import club.ttg.dnd5.domain.spell.service.SpellFilterService;
import club.ttg.dnd5.domain.common.model.Visibility;
import club.ttg.dnd5.domain.spell.service.SpellService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import club.ttg.dnd5.util.ContentPathUtils;
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
    @RequestMapping(path = "/{*url}", method = RequestMethod.HEAD)
    public Boolean isSpellExist(@PathVariable String url) {
        return spellService.existOrThrow(ContentPathUtils.normalizeUrl(url));
    }

    @Operation(summary = "Поиск заклинаний", description = "Поиск заклинаний с GET-параметрами фильтрации")
    @GetMapping("/search")
    public List<SpellShortResponse> search(@ParameterObject SpellQueryRequest request)
    {
        return spellService.search(request);
    }

    @GetMapping(value = "/{*url}", params = "!raw")
    public SpellDetailedResponse getSpellByUrl(@PathVariable String url) {
        return spellService.findDetailedByUrl(ContentPathUtils.normalizeUrl(url));
    }

    // Форма для редактирования. Раньше был отдельный путь /{url}/raw, но он несовместим с catch-all
    // {*url} (homebrew-url содержат слэши), поэтому raw-представление выбирается query-флагом ?raw.
    @GetMapping(value = "/{*url}", params = "raw")
    public SpellRequest getSpellFormByUrl(@PathVariable String url) {
        return spellService.findFormByUrl(ContentPathUtils.normalizeUrl(url));
    }

    @Operation(summary = "Получить метаданные фильтров", description = "Возвращает JSON для построения UI фильтров")
    @GetMapping("/filters")
    public FilterMetadataResponse getFilters(@RequestParam(required = false) Set<String> source) {
        return spellFilterService.getFilterMetadata(source != null ? source : Set.of());
    }

    @Secured({"ADMIN", "MODERATOR"})
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String createSpell(@RequestBody SpellRequest request) {
        return spellService.save(request);
    }

    @Operation(summary = "Создание пользовательского (homebrew) заклинания",
            description = "Создаёт заклинание от имени текущего авторизованного пользователя. "
                    + "Владелец и url (u/{handle}/{stem}) проставляются сервером; url из тела игнорируется.")
    @PostMapping("/homebrew")
    @ResponseStatus(HttpStatus.CREATED)
    public String createHomebrewSpell(@RequestBody SpellRequest request,
                                      @RequestParam(defaultValue = "PRIVATE") Visibility visibility) {
        return spellService.saveHomebrew(request, visibility);
    }

    @Operation(summary = "Предпросмотр заклинания")
    @Secured({"ADMIN", "MODERATOR"})
    @PostMapping("/preview")
    public SpellDetailedResponse preview(@RequestBody SpellRequest request) {
        return spellService.preview(request);
    }

    @Secured({"ADMIN", "MODERATOR"})
    @PutMapping("/{*url}")
    public String updateSpell(@PathVariable String url,
                                             @Valid
                                             @RequestBody SpellRequest request) {
        return spellService.update(ContentPathUtils.normalizeUrl(url), request);
    }

    @Secured({"ADMIN", "MODERATOR"})
    @DeleteMapping("/{*url}")
    public void deleteSpell(@PathVariable String url) {
        spellService.delete(ContentPathUtils.normalizeUrl(url));
    }
}
