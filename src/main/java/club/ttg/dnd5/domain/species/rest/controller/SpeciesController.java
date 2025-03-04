package club.ttg.dnd5.domain.species.rest.controller;

import club.ttg.dnd5.domain.species.rest.dto.SpeciesDetailResponse;
import club.ttg.dnd5.domain.species.rest.dto.SpeciesShortResponse;
import club.ttg.dnd5.domain.species.rest.dto.SpeciesRequest;
import club.ttg.dnd5.exception.EntityNotFoundException;
import club.ttg.dnd5.domain.species.service.SpeciesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

    @Operation(summary = "Проверить вид по URL", description = "Проверка вида по его уникальному URL.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Вид существует"),
            @ApiResponse(responseCode = "404", description = "Вид не существует")
    })
    @RequestMapping(path = "/{url}", method = RequestMethod.HEAD)
    public Boolean isSpecieExist(@PathVariable String url) {
        var exist = speciesService.exists(url);
        if(!exist) {
            throw new EntityNotFoundException("URL вида не существует");
        }
        else {
            return true;
        }
    }

    @PostMapping("/search")
    @Operation(summary = "Получение всех видов", description = "Виды будут не детальные, будет возвращать списков с указанным имени и урл")
    public List<SpeciesShortResponse> getAllSpecies() {
        return speciesService.getSpecies();
    }

    @Operation(summary = "Получить вид по URL", description = "Получение вида по его уникальному URL.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Вид успешно получен"),
            @ApiResponse(responseCode = "404", description = "Вид не найден")
    })
    @GetMapping("/{url}")
    @ResponseStatus(HttpStatus.OK)
    public SpeciesDetailResponse getSpeciesByUrl(@PathVariable String url) {
        return speciesService.findById(url);
    }

    @GetMapping("/lineages")
    @Operation(summary = "Получить подвиды по URL родительского вида",
            description = "Возвращает список подвидов, связанных с указанным родительским видом по его URL.")
    public List<SpeciesDetailResponse> getSubSpeciesByParentUrl(
            @Parameter(description = "URL родительского вида", required = true) @RequestParam String url) {
        return speciesService.getLineages(url);
    }

    @GetMapping("/related")
    @Operation(summary = "Получить все происхождения по URL",
            description = "Возвращает список всех происхождений, включая родительский вид и подвиды по указанному URL подвида.")
    public List<SpeciesDetailResponse> getAllRelatedSpeciesBySubSpeciesUrl(
            @Parameter(description = "URL происхождения", required = true)
            @RequestParam String url) {
        return speciesService.getAllLineages(url);
    }

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
    @PostMapping("/parent")
    public SpeciesDetailResponse addParent(
            @Parameter(description = "URL вида, к которому добавляется родитель", required = true) @RequestParam String speciesUrl,
            @Parameter(description = "URL родителя, который будет добавлен", required = true) @RequestParam String speciesParentUrl) {
        return speciesService.addParent(speciesUrl, speciesParentUrl);
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
    @PostMapping("/subspecies")
    public SpeciesDetailResponse addSubSpecies(
            @Parameter(description = "URL вида, к которому добавляются подвиды", required = true) @RequestParam String speciesUrl,
            @Parameter(description = "Список URL подвидов, которые будут добавлены", required = true) @RequestBody List<String> subSpeciesUrls) {
        return speciesService.addSubSpecies(speciesUrl, subSpeciesUrls);
    }

    @Secured("ADMIN")
    @Operation(summary = "Создать новый вид", description = "Создание нового вида в системе.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Вид успешно создан"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SpeciesDetailResponse createSpecies(@RequestBody SpeciesRequest request) {
        return speciesService.save(request);
    }

    @Operation(summary = "Обновить существующий вид", description = "Обновление данных существующего вида.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Вид успешно обновлен"),
            @ApiResponse(responseCode = "404", description = "Вид не найден"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    @PutMapping("/{url}")
    @Secured("ADMIN")
    @ResponseStatus(HttpStatus.OK)
    public SpeciesDetailResponse updateSpecies(@PathVariable String url, @RequestBody SpeciesRequest request) {
        return speciesService.update(url, request);
    }
}
