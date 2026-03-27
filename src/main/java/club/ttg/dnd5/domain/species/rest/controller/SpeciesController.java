package club.ttg.dnd5.domain.species.rest.controller;


import club.ttg.dnd5.domain.species.rest.dto.SpeciesDetailResponse;
import club.ttg.dnd5.domain.species.rest.dto.SpeciesShortResponse;
import club.ttg.dnd5.domain.species.rest.dto.SpeciesRequest;
import club.ttg.dnd5.domain.species.service.SpeciesFilterService;
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

import java.util.Collection;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/species")
@Tag(name = "Виды", description = "API для управления видами")
public class SpeciesController {
    private final SpeciesService speciesService;
    private final SpeciesFilterService speciesFilterService;

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



    @Operation(summary = "Получить метаданные фильтров")
    @GetMapping("/filters")
    public club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse getFilters() {
        return speciesFilterService.getFilterMetadata();
    }



    @Operation(summary = "Поиск видов", description = "Поиск видов с GET-параметрами фильтрации")
    @GetMapping("/search")
    public List<SpeciesShortResponse> search(
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam java.util.Map<String, String[]> params)
    {
        var request = new club.ttg.dnd5.domain.species.rest.dto.SpeciesQueryRequest();
        request.setSearch(search);
        if (page != null) request.setPage(page);
        if (size != null) request.setPageSize(size);
        request.setCreatureType(club.ttg.dnd5.domain.filter.rest.QueryParamFilterResolver.resolveEnum(params, "creatureType", club.ttg.dnd5.domain.common.dictionary.CreatureType.class));
        request.setSource(club.ttg.dnd5.domain.filter.rest.QueryParamFilterResolver.resolveSources(params, "source"));
        return speciesService.search(request);
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
    @Operation(summary = "Получить все подвиды",
            description = "Возвращает список подвидов, связанных с указанным родительским видом по его URL.")
    public List<SpeciesShortResponse> getLineages() {
        return speciesService.getLineages();
    }

    @GetMapping("/{url}/lineages")
    @Operation(summary = "Получить подвиды по URL родительского вида",
            description = "Возвращает список подвидов, связанных с указанным родительским видом по его URL.")
    public List<SpeciesDetailResponse> getSubSpeciesByParentUrl(
            @Parameter(description = "URL родительского вида", required = true) @PathVariable String url) {
        return speciesService.getLineages(url);
    }

    @GetMapping("/{url}/lineages/search")
    @Operation(summary = "Получить все происхождения по URL",
            description = "Возвращает список всех происхождений, включая родительский вид и подвиды по указанному URL подвида.")
    public Collection<SpeciesShortResponse> getAllRelatedSpeciesBySubSpeciesUrl(
            @Parameter(description = "URL происхождения", required = true)
            @PathVariable String url) {
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
            @Parameter(description = "URL происхождения, к которому добавляется родитель", required = true) @RequestParam String speciesUrl,
            @Parameter(description = "URL вида, который будет добавлен", required = true) @RequestParam String speciesParentUrl) {
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
    public String createSpecies(@RequestBody SpeciesRequest request) {
        return speciesService.save(request);
    }

    @Operation(summary = "Предпросмотр вида")
    @Secured("ADMIN")
    @PostMapping("/preview")
    public SpeciesDetailResponse preview(@RequestBody SpeciesRequest request) {
        return speciesService.preview(request);
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
    public String updateSpecies(@PathVariable String url, @RequestBody SpeciesRequest request) {
        return speciesService.update(url, request);
    }

    @GetMapping("/{url}/raw")
    public SpeciesRequest getSpeciesFormByUrl(@PathVariable String url) {
        return speciesService.findFormByUrl(url);
    }
}
