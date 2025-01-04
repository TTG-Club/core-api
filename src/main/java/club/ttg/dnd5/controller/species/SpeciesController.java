package club.ttg.dnd5.controller.species;

import club.ttg.dnd5.dto.species.CreateSpeciesDto;
import club.ttg.dnd5.dto.species.SpeciesDto;
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

    @PostMapping("/search")
    @Operation(summary = "Получение всех видов", description = "Виды будут не детальные, будет возвращать списков с указанным имени и урл")
    public ResponseEntity<List<SpeciesDto>> getAllSpecies() {
        return ResponseEntity.ok(speciesService.getAllSpecies());
    }

    @Operation(summary = "Получить вид по URL", description = "Получение вида по его уникальному URL.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Вид успешно получен"),
            @ApiResponse(responseCode = "404", description = "Вид не найден")
    })
    @GetMapping("/{url}")
    @ResponseStatus(HttpStatus.OK)
    public SpeciesDto getSpeciesByUrl(@PathVariable String url) {
        return speciesService.findById(url);
    }

    @Operation(summary = "Проверить вид по URL", description = "Проверка вида по его уникальному URL.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Вид успешно получен"),
            @ApiResponse(responseCode = "409", description = "Вид не найден")
    })
    @RequestMapping(path = "/{url}", method = RequestMethod.HEAD)
    public ResponseEntity<?> isSpecieExist(@PathVariable String url) {
        Boolean exist = speciesService.isExist(url);

        if (exist.equals(Boolean.TRUE)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/subspecies")
    @Operation(summary = "Получить подвиды по URL родительского вида",
            description = "Возвращает список подвидов, связанных с указанным родительским видом по его URL.")
    public ResponseEntity<List<SpeciesDto>> getSubSpeciesByParentUrl(
            @Parameter(description = "URL родительского вида", required = true) @RequestParam String parentUrl) {
        List<SpeciesDto> subSpeciesDto = speciesService.getSubSpeciesByParentUrl(parentUrl);
        return ResponseEntity.ok(subSpeciesDto);
    }

    @GetMapping("/related")
    @Operation(summary = "Получить все связанные виды по URL подвида",
            description = "Возвращает список всех связанных видов, включая родительский вид и подвиды по указанному URL подвида.")
    public ResponseEntity<List<SpeciesDto>> getAllRelatedSpeciesBySubSpeciesUrl(
            @Parameter(description = "URL подвига", required = true) @RequestParam String subSpeciesUrl) {
        List<SpeciesDto> relatedSpeciesRespons = speciesService.getAllRelatedSpeciesBySubSpeciesUrl(subSpeciesUrl);
        return ResponseEntity.ok(relatedSpeciesRespons);
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
    public ResponseEntity<SpeciesDto> addParent(
            @Parameter(description = "URL вида, к которому добавляется родитель", required = true) @RequestParam String speciesUrl,
            @Parameter(description = "URL родителя, который будет добавлен", required = true) @RequestParam String speciesParentUrl) {
        SpeciesDto response = speciesService.addParent(speciesUrl, speciesParentUrl);
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
    @PostMapping("/subspecies")
    public ResponseEntity<SpeciesDto> addSubSpecies(
            @Parameter(description = "URL вида, к которому добавляются подвиды", required = true) @RequestParam String speciesUrl,
            @Parameter(description = "Список URL подвидов, которые будут добавлены", required = true) @RequestBody List<String> subSpeciesUrls) {
        SpeciesDto response = speciesService.addSubSpecies(speciesUrl, subSpeciesUrls);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Создать новый вид", description = "Создание нового вида в системе.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Вид успешно создан"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    @PostMapping("/new")
    @Secured("ROLE_ADMIN")
    @ResponseStatus(HttpStatus.CREATED)
    public SpeciesDto createSpecies(@RequestBody CreateSpeciesDto createSpeciesDTO) {
        return speciesService.save(createSpeciesDTO);
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
    public SpeciesDto updateSpecies(@PathVariable String url, @RequestBody SpeciesDto speciesDTO) {
        return speciesService.update(url, speciesDTO);
    }
}
