package club.ttg.dnd5.domain.beastiary.rest.controller;

import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureDetailResponse;
import club.ttg.dnd5.domain.beastiary.rest.dto.BeastRequest;
import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureShortResponse;
import club.ttg.dnd5.domain.beastiary.service.CreatureService;
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

@Tag(name = "Бестиарий", description = "REST API для существ из бестиария")

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/bestiary")
public class CreatureController {
    private final CreatureService creatureService;

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
                                              @Size(min = 3)
                                              @Schema( description = "Строка поиска, если null-отдаются все сущности")
                                              String searchLine) {
        return creatureService.search(searchLine);
    }

    @Operation(summary = "Получение детальной информации по URL", description = "Получение детальной информации по его уникальному URL.")
    @GetMapping("/{url}")
    public CreatureDetailResponse getByUrl(@PathVariable String url) {
        return creatureService.findDetailedByUrl(url);
    }

    @GetMapping("/{url}/update")
    public BeastRequest getFormByUrl(@PathVariable String url) {
        return creatureService.findFormByUrl(url);
    }

    @Operation(summary = "Добавление существа")
    @Secured("ADMIN")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String create(@RequestBody BeastRequest request) {
        return creatureService.save(request);
    }

    @Operation(summary = "Обновление существа")
    @Secured("ADMIN")
    @PutMapping("/{url}")
    public String update(@PathVariable String url,
                                      @Valid
                                      @RequestBody BeastRequest request) {
        return creatureService.update(url, request);
    }

    @Operation(summary = "Сокрытие существа")
    @Secured("ADMIN")
    @DeleteMapping("/{url}")
    public String delete(@PathVariable String url) {
        return creatureService.delete(url);
    }
}
