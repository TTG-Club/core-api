package club.ttg.dnd5.domain.beastiary.rest.controller;

import club.ttg.dnd5.domain.beastiary.rest.dto.section.CreatureSectionDetailResponse;
import club.ttg.dnd5.domain.beastiary.rest.dto.section.CreatureSectionShortResponse;
import club.ttg.dnd5.domain.beastiary.rest.dto.section.CretureSectionRequest;
import club.ttg.dnd5.domain.beastiary.service.CreatureSectionService;
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

@Tag(name = "Бестиарий - Секции", description = "REST API для секций существ из бестиария")

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/creature-section")
public class CreatureSectionController {
    private final CreatureSectionService sectionService;

    @Operation(summary = "Проверить секцию по URL", description = "Проверка секции по его уникальному URL.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Секция существует"),
            @ApiResponse(responseCode = "404", description = "Секция не существует")
    })
    @RequestMapping(path = "/{url}", method = RequestMethod.HEAD)
    public Boolean isSpellExist(@PathVariable String url) {
        return sectionService.existOrThrow(url);
    }

    @Operation(summary = "Поиск существ", description = "Поиск секции по именам")
    @PostMapping("/search")
    public List<CreatureSectionShortResponse> search(@RequestParam(name = "query", required = false)
                                              @Valid
                                              @Size(min = 3)
                                              @Schema( description = "Строка поиска, если null-отдаются все сущности")
                                              String searchLine) {
        return sectionService.search(searchLine);
    }

    @Operation(summary = "Получение детальной информации по URL", description = "Получение детальной информации по его уникальному URL.")
    @GetMapping("/{url}")
    public CreatureSectionDetailResponse getByUrl(@PathVariable String url) {
        return sectionService.findDetailedByUrl(url);
    }

    @GetMapping("/{url}/update")
    public CretureSectionRequest getFormByUrl(@PathVariable String url) {
        return sectionService.findFormByUrl(url);
    }

    @Operation(summary = "Добавление секции")
    @Secured("ADMIN")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String create(@RequestBody CretureSectionRequest request) {
        return sectionService.save(request);
    }

    @Operation(summary = "Обновление секции")
    @Secured("ADMIN")
    @PutMapping("/{url}")
    public String update(@PathVariable String url,
                                      @Valid
                                      @RequestBody CretureSectionRequest request) {
        return sectionService.update(url, request);
    }

    @Operation(summary = "Сокрытие секции")
    @Secured("ADMIN")
    @DeleteMapping("/{url}")
    public String delete(@PathVariable String url) {
        return sectionService.delete(url);
    }
}
