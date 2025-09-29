package club.ttg.dnd5.domain.character_class.rest.controller;

import club.ttg.dnd5.domain.character_class.rest.dto.ClassDetailedResponse;
import club.ttg.dnd5.domain.character_class.rest.dto.ClassRequest;
import club.ttg.dnd5.domain.character_class.rest.dto.ClassShortResponse;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/classes")
@Tag(name = "API для Классов", description = "API для управления классами")
public class ClassController {
    private final ClassService classService;

    @Operation(summary = "Проверить класс по URL", description = "Проверка класса по его уникальному URL.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Класс существует"),
            @ApiResponse(responseCode = "404", description = "Класс не существует")
    })
    @RequestMapping(path = "/{url}", method = RequestMethod.HEAD)
    public Boolean isSpecieExist(@PathVariable String url) {
        if(!classService.exists(url)) {
            throw new EntityNotFoundException("URL класса не существует");
        }
        else {
            return true;
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Получение всех классов", description = "Классы будут не детальные, будет возвращать списков с указанным имени и url")
    public List<ClassShortResponse> getAllClasses() {
        return classService.findAllClasses();
    }

    @GetMapping("/{url}")
    public ClassDetailedResponse getClassByUrl(@PathVariable String url) {
        return classService.findDetailedByUrl(url);
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
