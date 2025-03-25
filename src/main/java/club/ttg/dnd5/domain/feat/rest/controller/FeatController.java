package club.ttg.dnd5.domain.feat.rest.controller;

import club.ttg.dnd5.domain.common.rest.dto.ShortResponse;
import club.ttg.dnd5.domain.feat.rest.dto.FeatDetailResponse;
import club.ttg.dnd5.domain.feat.rest.dto.FeatRequest;

import club.ttg.dnd5.domain.feat.rest.dto.FeatShortResponse;
import club.ttg.dnd5.domain.feat.service.FeatService;
import club.ttg.dnd5.exception.EntityExistException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/feat")
@Tag(name = "Черты ", description = "REST API черт персонажа")
public class FeatController {
    private final FeatService featService;

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(path = "/{featUrl}", method = RequestMethod.HEAD)
    public ResponseEntity<Boolean> existByUrl(@PathVariable final String featUrl) {
        if (featService.exists(featUrl)) {
            throw new EntityExistException("Черта существуют с URL: " + featUrl);
        }
        return ResponseEntity.ok(false);
    }

    @Operation(summary = "Получение детального описания черты")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Черта успешно получена"),
            @ApiResponse(responseCode = "404", description = "Черта не найдена")
    })

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{featUrl}")
    public FeatDetailResponse getFeat(@PathVariable final String featUrl) {
        return featService.getFeat(featUrl);
    }

    @Secured("ADMIN")
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
    @PostMapping("{featUrl}")
    public String updateFeats(@PathVariable final String featUrl,
                                          @RequestBody final FeatRequest featDto) {
        return featService.updateFeat(featUrl, featDto);
    }

    @Secured("ADMIN")
    @Operation(summary = "Скрывает черту")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Черта удалена из общего списка"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("{featUrl}")
    public String deleteFeats(@PathVariable final String featUrl) {
        return featService.delete(featUrl);
    }
}
