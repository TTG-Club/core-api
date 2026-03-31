package club.ttg.dnd5.domain.character_class.rest.controller;

import org.springdoc.core.annotations.ParameterObject;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse;
import club.ttg.dnd5.domain.character_class.rest.dto.ClassAbilityImprovementResponse;
import club.ttg.dnd5.domain.character_class.rest.dto.ClassDetailedResponse;
import club.ttg.dnd5.domain.character_class.rest.dto.ClassQueryRequest;
import club.ttg.dnd5.domain.character_class.rest.dto.ClassRequest;
import club.ttg.dnd5.domain.character_class.rest.dto.ClassShortResponse;
import club.ttg.dnd5.domain.character_class.service.ClassFilterService;
import club.ttg.dnd5.domain.character_class.service.ClassService;

import club.ttg.dnd5.exception.EntityNotFoundException;
import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/classes")
@Tag(name = "Классы", description = "API для управления классами")
public class ClassController {
    private final ClassService classService;
    private final ClassFilterService classFilterService;

    @Operation(summary = "Проверить класс по URL", description = "Проверка класса по его уникальному URL.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Класс существует"),
            @ApiResponse(responseCode = "404", description = "Класс не существует")
    })
    @RequestMapping(path = "/{url}", method = RequestMethod.HEAD)
    public Boolean isClassExist(@PathVariable String url) {
        if(!classService.exists(url)) {
            throw new EntityNotFoundException("URL класса не существует");
        }
        else {
            return true;
        }
    }

    @Operation(summary = "Поиск классов", description = "Поиск классов с GET-параметрами фильтрации")
    @GetMapping("/search")
    public List<ClassShortResponse> search(
            @ParameterObject @ModelAttribute ClassQueryRequest request)
    {
        return classService.search(request);
    }

    @GetMapping("/{url}")
    public ClassDetailedResponse getClassByUrl(@PathVariable String url) {
        return classService.findDetailedByUrl(url);
    }

    @Operation(summary = "Получить метаданные фильтров")
    @GetMapping("/filters")
    public FilterMetadataResponse getFilters(@RequestParam(required = false) Set<String> source) {
        return classFilterService.getFilterMetadata(source != null ? source : Set.of());
    }

    @GetMapping("/subclasses")
    public List<ClassShortResponse> getSubClasses() {
        return classService.getSubclasses();
    }

    @GetMapping("/{parentUrl}/subclasses")
    public List<ClassShortResponse> getSubClassesByParentUrl(@PathVariable String parentUrl) {
        return classService.getSubclasses(parentUrl);
    }

    @Operation(summary = "Получение уровней улучшения характеристик класса")
    @GetMapping("/ability-improvement")
    public List<ClassAbilityImprovementResponse> getAbilityImprovements() {
        return classService.getAbilityImprovements();
    }

    @Secured("ADMIN")
    @Operation(summary = "Создать новый класс", description = "Создание нового класса в системе.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Класс успешно создан"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClassDetailedResponse createClass(@RequestBody ClassRequest request) {
        return classService.save(request);
    }

    @Secured("ADMIN")
    @PutMapping("/{url}")
    public String updateClass(@PathVariable String url,
                              @Valid
                              @RequestBody ClassRequest request) {
        return classService.update(url, request);
    }

    @GetMapping("/{url}/raw")
    public ClassRequest getClassFormByUrl(@PathVariable String url) {
        return classService.findFormByUrl(url);
    }

    @Operation(summary = "Предпросмотр класса")
    @Secured("ADMIN")
    @PostMapping("/preview")
    public ClassDetailedResponse preview(@RequestBody ClassRequest request) {
        return classService.preview(request);
    }
}
