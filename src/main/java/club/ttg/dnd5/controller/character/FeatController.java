package club.ttg.dnd5.controller.character;

import club.ttg.dnd5.dto.character.FeatDto;
import club.ttg.dnd5.exception.EntityExistException;
import club.ttg.dnd5.service.character.FeatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
            throw new EntityExistException();
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
    public FeatDto getFeat(@PathVariable final String featUrl) {
        return featService.getFeat(featUrl);
    }

    @Secured("ADMIN")
    @Operation(summary = "Получение списка краткого описания черты")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Черты успешно получена")
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/search")
    public Collection<FeatDto> getFeats() {
        return featService.getFeats();
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
    public FeatDto addFeats(@RequestBody final FeatDto featDto) {
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
    public FeatDto updateFeats(@PathVariable final String featUrl,
                               @RequestBody final FeatDto featDto) {
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
    public FeatDto deleteFeats(@PathVariable final String featUrl) {
        return featService.delete(featUrl);
    }
}
