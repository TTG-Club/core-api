package club.ttg.dnd5.controller.species;

import club.ttg.dnd5.dto.engine.SearchRequest;
import club.ttg.dnd5.dto.species.SpeciesResponse;
import club.ttg.dnd5.service.species.SpeciesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/species")
@Tag(name = "API для Видов", description = "API для управления видами")
public class SpeciesController {
    private final SpeciesService speciesService;

    @Operation(summary = "Получить вид по URL", description = "Получение вида по его уникальному URL.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Вид успешно получен"),
            @ApiResponse(responseCode = "404", description = "Вид не найден")
    })
    @GetMapping("/{url}")
    @ResponseStatus(HttpStatus.OK)
    public SpeciesResponse getSpeciesByUrl(@PathVariable String url) {
        return speciesService.findById(url);
    }

    @Operation(summary = "Создать новый вид", description = "Создание нового вида в системе.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Вид успешно создан"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    @PostMapping
    @Secured("ROLE_ADMIN")
    @ResponseStatus(HttpStatus.CREATED)
    public SpeciesResponse createSpecies(@RequestBody SpeciesResponse speciesResponse) {
        return speciesService.save(speciesResponse);
    }

    @Operation(summary = "Обновить существующий вид", description = "Обновление данных существующего вида.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Вид успешно обновлен"),
            @ApiResponse(responseCode = "404", description = "Вид не найден"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    @PutMapping("/{url}")
    @Secured("ROLE_ADMIN")
    @ResponseStatus(HttpStatus.OK)
    public SpeciesResponse updateSpecies(@PathVariable String url, @RequestBody SpeciesResponse speciesResponse) {
        speciesResponse.setUrl(url);
        return speciesService.update(speciesResponse);
    }

    @Operation(summary = "Поиск видов", description = "Поиск видов по различным фильтрам и критериям.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Результаты поиска успешно получены")
    })
    @PostMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public List<SpeciesResponse> searchSpecies(@RequestBody SearchRequest request) {
        return speciesService.searchSpecies(request);
    }
}
