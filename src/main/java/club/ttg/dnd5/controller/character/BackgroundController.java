package club.ttg.dnd5.controller.character;

import club.ttg.dnd5.dto.character.BackgroundDto;
import club.ttg.dnd5.exception.EntityNotFoundException;
import club.ttg.dnd5.service.character.BackgroundService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v2/background")
@Tag(name = "Предыстории", description = "REST API предысторий персонажа")
public class BackgroundController {
    private final BackgroundService backgroundService;

    @GetMapping("{backgroundUrl}")
    public BackgroundDto findBackground(@PathVariable final String backgroundUrl) {
        return backgroundService.getBackground(backgroundUrl);
    }

    @RequestMapping(path = "/{backgroundUrl}", method = RequestMethod.HEAD)
    public boolean existByUrl(@PathVariable final String backgroundUrl) {
        var exist = backgroundService.exists(backgroundUrl);
        if (!exist) {
            throw new EntityNotFoundException("URL предыстории не найден");
        }
        return true;
    }

    @PostMapping("/search")
    public Collection<BackgroundDto> findBackgrounds() {
        return backgroundService.getBackgrounds();
    }

    @Secured("ADMIN")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public BackgroundDto addBackgrounds(@RequestBody final BackgroundDto backgroundDto) {
        return backgroundService.addBackground(backgroundDto);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Предыстория успешно обновлена"),
            @ApiResponse(responseCode = "404", description = "Предыстория не найден"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    @Secured("ADMIN")
    @PutMapping("{backgroundUrl}")
    public BackgroundDto updateBackgrounds(
            @PathVariable final String backgroundUrl,
            @RequestBody final BackgroundDto backgroundDto) {
        return backgroundService.updateBackgrounds(backgroundUrl, backgroundDto);
    }

    @Secured("ADMIN")
    @DeleteMapping("{backgroundUrl}")
    public BackgroundDto deleteBackgrounds(
            @PathVariable final String backgroundUrl) {
        return backgroundService.deleteBackgrounds(backgroundUrl);
    }
}
