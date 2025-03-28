package club.ttg.dnd5.domain.glossary.controller;

import club.ttg.dnd5.domain.glossary.rest.dto.GlossaryDetailedResponse;
import club.ttg.dnd5.domain.glossary.rest.dto.GlossaryShortResponse;
import club.ttg.dnd5.domain.glossary.rest.dto.create.GlossaryRequest;
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

import java.util.List;

@Tag(name = "Глоссарий", description = "REST API глоссарий")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/glossary")
public class GlossaryController {
    private final GlossaryService glossaryService;

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
                                              @Size(min = 3)
                                              @Schema( description = "Строка поиска, если null-отдаются все сущности")
                                              String searchLine) {
        return glossaryService.search(searchLine);
    }

    @GetMapping("/{url}")
    public GlossaryDetailedResponse getGlossaryByUrl(@PathVariable String url) {
        return glossaryService.findDetailedByUrl(url);
    }

    public GlossaryRequest findGlossaryFormByUrl(final String url) {
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
}
