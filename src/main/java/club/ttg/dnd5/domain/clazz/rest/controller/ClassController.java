package club.ttg.dnd5.domain.clazz.rest.controller;

import club.ttg.dnd5.domain.clazz.rest.dto.ClassDetailResponse;
import club.ttg.dnd5.domain.clazz.rest.dto.ClassFeatureDto;
import club.ttg.dnd5.dto.engine.SearchRequest;
import club.ttg.dnd5.exception.EntityNotFoundException;
import club.ttg.dnd5.domain.clazz.service.ClassService;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/class")
@Tag(name = "Классы", description = "REST API классов персонажа")
public class ClassController {
    private final ClassService classService;

    @Operation(summary = "Проверить класс по URL", description = "Проверка класса по его уникальному URL.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Класс существует"),
            @ApiResponse(responseCode = "404", description = "Класс не существует")
    })
    @RequestMapping(path = "/{url}", method = RequestMethod.HEAD)
    public Boolean isSpecieExist(@PathVariable String url) {
        var exist = classService.exist(url);
        if(!exist) {
            throw new EntityNotFoundException("URL вида не существует");
        }
        return true;
    }

    @Operation(summary = "Получение краткого списка классов")
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/search")
    public Collection<ClassDetailResponse> getAllClasses(final SearchRequest request) {
        return classService.getClasses(request);
    }

    @Operation(summary = "Получение краткого списка подклассов для класса")
    @GetMapping("/{parentUrl}/subclasses")
    public Collection<ClassDetailResponse> getSubclasses(@PathVariable String parentUrl) {
        return classService.getSubClasses(parentUrl);
    }

    @Operation(summary = "Получение детального описания класса")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Класс успешно получен"),
            @ApiResponse(responseCode = "404", description = "Класс не найден")
    })
    @GetMapping("/{url}")
    public ClassDetailResponse getClass(@PathVariable String url) {
        return classService.getClass(url);
    }

    @Secured("ADMIN")
    @Operation(summary = "Добавление класса или подкласса")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Класс успешно создан"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ClassDetailResponse addClass(@RequestBody final ClassDetailResponse request) {
        return classService.addClass(request);
    }

    /**
     * Метод для добавления подкласса к классу.
     *
     * @param classUrl      URL класса, к которому добавляется родитель.
     * @param classParentUrl URL родителя, который будет добавлен.
     * @return информацией о виде.
     */
    @Secured("ADMIN")
    @Operation(summary = "Добавить родителя к классу", description = "Добавляет родителя к указанному классу по его URL.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Родитель успешно добавлен к классу"),
            @ApiResponse(responseCode = "404", description = "Класс или родитель не найден")
    })
    @PostMapping("/{classUrl}/parent")
    public ClassDetailResponse addParent(
            @Parameter(description = "URL класса, к которому добавляется родитель") @PathVariable String classUrl,
            @Parameter(description = "URL родителя, который будет добавлен") @RequestParam String classParentUrl) {
        return classService.addParent(classUrl, classParentUrl);
    }

    /**
     * Метод для добавления подкласса к классу.
     *
     * @param classUrl      URL класса, к которому добавляется умение.
     * @param featureDto умение, которое будет добавлено к классу.
     * @return информацией о виде.
     */
    @Secured("ADMIN")
    @Operation(summary = "Добавить умение к классу", description = "Добавляет умение к указанному классу по его URL.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Умение успешно добавлен к классу"),
            @ApiResponse(responseCode = "404", description = "Класс не найден")
    })
    @PostMapping("/{classUrl}/feature")
    public ClassDetailResponse addFeature(
            @Parameter(description = "URL класса, к которому добавляется умение") @PathVariable String classUrl,
            @RequestBody ClassFeatureDto featureDto) {
        return classService.addFeature(classUrl, featureDto);
    }

    @Secured("ADMIN")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Класс успешно обновлен"),
            @ApiResponse(responseCode = "404", description = "Класс не найден"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    @PutMapping("/{url}")
    public ClassDetailResponse updateClass(
            @PathVariable final String url,
            @RequestBody final ClassDetailResponse request) {
        return classService.updateClass(url, request);
    }
}
