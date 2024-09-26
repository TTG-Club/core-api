package club.ttg.dnd5.controller.species;

import club.ttg.dnd5.dto.engine.SearchRequest;
import club.ttg.dnd5.dto.species.CreateSpeciesDTO;
import club.ttg.dnd5.dto.species.SpeciesResponse;
import club.ttg.dnd5.service.species.SpeciesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/species")
@Tag(name = "API для Видов", description = "API для управления видами")
public class SpeciesController {
    private final SpeciesService speciesService;

    /**
     * Метод для добавления родителя к виду.
     *
     * @param speciesUrl      URL вида, к которому добавляется родитель.
     * @param speciesParentUrl URL родителя, который будет добавлен.
     * @return ResponseEntity с обновленной информацией о виде.
     */
    @Operation(summary = "Добавить родителя к виду", description = "Добавляет родителя к указанному виду по его URL.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Родитель успешно добавлен к виду"),
            @ApiResponse(responseCode = "404", description = "Вид или родитель не найден")
    })
    @PostMapping("/{speciesUrl}/parent")
    public ResponseEntity<SpeciesResponse> addParent(
            @Parameter(description = "URL вида, к которому добавляется родитель") @PathVariable String speciesUrl,
            @Parameter(description = "URL родителя, который будет добавлен") @RequestParam String speciesParentUrl) {
        SpeciesResponse response = speciesService.addParent(speciesUrl, speciesParentUrl);
        return ResponseEntity.ok(response);
    }

    /**
     * Метод для добавления подвидов к виду.
     *
     * @param speciesUrl URL вида, к которому добавляются подвиды.
     * @param subSpeciesUrls Список URL подвидов, которые будут добавлены.
     * @return ResponseEntity с обновленной информацией о виде.
     */
    @Operation(summary = "Добавить подвиды к виду", description = "Добавляет указанные подвиды к указанному виду.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Подвиды успешно добавлены к виду"),
            @ApiResponse(responseCode = "404", description = "Вид не найден")
    })
    @PostMapping("/{speciesUrl}/subspecies")
    public ResponseEntity<SpeciesResponse> addSubSpecies(
            @Parameter(description = "URL вида, к которому добавляются подвиды") @PathVariable String speciesUrl,
            @Parameter(description = "Список URL подвидов, которые будут добавлены") @RequestBody List<String> subSpeciesUrls) {
        SpeciesResponse response = speciesService.addSubSpecies(speciesUrl, subSpeciesUrls);
        return ResponseEntity.ok(response);
    }

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
    public SpeciesResponse createSpecies(@RequestBody CreateSpeciesDTO createSpeciesDTO) {
        return speciesService.save(createSpeciesDTO);
    }

    @Operation(summary = "Обновить существующий вид", description = "Обновление данных существующего вида.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Вид успешно обновлен"),
            @ApiResponse(responseCode = "404", description = "Вид не найден"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    @PutMapping("/{oldUrl}")
    @Secured("ROLE_ADMIN")
    @ResponseStatus(HttpStatus.OK)
    public SpeciesResponse updateSpecies(@PathVariable String oldUrl, @RequestBody SpeciesResponse speciesResponse) {
        return speciesService.update(oldUrl, speciesResponse);
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
