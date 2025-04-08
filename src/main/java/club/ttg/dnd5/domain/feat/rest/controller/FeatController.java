package club.ttg.dnd5.domain.feat.rest.controller;

import club.ttg.dnd5.domain.feat.rest.dto.FeatDetailResponse;
import club.ttg.dnd5.domain.feat.rest.dto.FeatRequest;

import club.ttg.dnd5.domain.feat.rest.dto.FeatShortResponse;
import club.ttg.dnd5.domain.feat.service.FeatService;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/feats")
@Tag(name = "Черты ", description = "REST API черт персонажа")
public class FeatController {
    private final FeatService featService;

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

    @Operation(summary = "Получение списка черт")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Черты успешно получена")
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/search")
    public Collection<FeatShortResponse> getFeats(@RequestParam(name = "query", required = false)
                                                  @Valid
                                                  @Size(min = 3)
                                                  @Schema( description = "Строка поиска, если null-отдаются все сущности")
                                                  String searchLine) {
        return featService.getFeats(searchLine);
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

    @Secured("ADMIN")
    @Operation(summary = "Обновление черты")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Черта успешно обновлена"),
            @ApiResponse(responseCode = "200", description = "Черта не найдена"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("{url}")
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
