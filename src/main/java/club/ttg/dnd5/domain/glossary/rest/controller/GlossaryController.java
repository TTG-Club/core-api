package club.ttg.dnd5.domain.glossary.rest.controller;

import club.ttg.dnd5.domain.filter.model.FilterInfo;
import club.ttg.dnd5.domain.glossary.rest.dto.GlossaryDetailedResponse;
import club.ttg.dnd5.domain.glossary.rest.dto.GlossaryShortResponse;
import club.ttg.dnd5.domain.glossary.rest.dto.create.GlossaryRequest;
import club.ttg.dnd5.domain.glossary.service.GlossaryFilterService;
import club.ttg.dnd5.domain.glossary.service.GlossaryService;
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
import club.ttg.dnd5.domain.filter.model.SearchBody;

import java.util.List;

@Tag(name = "Глоссарий", description = "REST API глоссарий")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/glossary")
public class GlossaryController {
    private final GlossaryService glossaryService;
    private final GlossaryFilterService glossaryFilterService;

    @Operation(summary = "Проверить глоссарий по URL", description = "Проверка записи глоссария по его уникальному URL")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Запись существует"),
            @ApiResponse(responseCode = "404", description = "Запись не существует")
    })
    @RequestMapping(path = "/{url}", method = RequestMethod.HEAD)
    public Boolean isGlossaryExist(@PathVariable String url) {
        return glossaryService.existOrThrow(url);
    }

    @Operation(summary = "Поиск записи глоссария", description = "Поиск записи глоссария по именам")
    @PostMapping("/search")
    public List<GlossaryShortResponse> getGlossary(@RequestParam(name = "query", required = false)
                                              @Valid
                                              @Size(min = 2)
                                              @Schema( description = "Строка поиска, если null-отдаются все сущности")
                                              String searchLine,
                                              @RequestBody(required = false) SearchBody searchBody){
        return glossaryService.search(searchLine, searchBody);
    }

    @GetMapping("/{url}")
    public GlossaryDetailedResponse getGlossaryByUrl(@PathVariable String url) {
        return glossaryService.findDetailedByUrl(url);
    }

    @GetMapping("/{url}/raw")
    public GlossaryRequest getFormByUrl(@PathVariable String url) {
        return glossaryService.findFormByUrl(url);
    }

    @Secured("ADMIN")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String createGlossary(@RequestBody GlossaryRequest request) {
        return glossaryService.save(request);
    }

    @Secured("ADMIN")
    @PutMapping("/{url}")
    public String updateGlossary(@PathVariable String url, @Valid @RequestBody GlossaryRequest request) {
        return glossaryService.update(url, request);
    }

    @Secured("ADMIN")
    @DeleteMapping("/{url}")
    public void deleteGlossary(@PathVariable String url) {
        glossaryService.delete(url);
    }

    @GetMapping("/filters")
    public FilterInfo getFilters() {
        return glossaryFilterService.getDefaultFilterInfo();
    }
}
