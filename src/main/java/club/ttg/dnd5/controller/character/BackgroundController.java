package club.ttg.dnd5.controller.character;

import club.ttg.dnd5.dto.character.BackgroundDto;
import club.ttg.dnd5.service.character.BackgroundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v2/background")
@Tag(name = "Предыстории", description = "REST API предысторий персонажа")
public class BackgroundController {
    private final BackgroundService backgroundService;

    /**
     * Проверка существования предыстории по URL.
     *
     * @param url URL предыстории.
     * @return 204, если предыстории с таким URL не существует; 409, если вид существует.
     */
    @Operation(
            summary = "Проверка существования предыстории",
            description = "Возвращает 204 (No Content), если предыстория с указанным URL не существует, или 409 (Conflict), если существует."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Предыстория с указанным URL не найден."),
            @ApiResponse(responseCode = "409", description = "Предыстория с указанным URL уже существует.")
    })
    @RequestMapping(value = "/{url}", method = RequestMethod.HEAD)
    public ResponseEntity<Void> handleOptions(@PathVariable("url") String url) {
        boolean exists = backgroundService.existByUrl(url);
        if (exists) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } else {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
    }

    @GetMapping("/{url}")
    public BackgroundDto findBackground(@PathVariable final String url) {
        return backgroundService.getBackground(url);
    }

    @PostMapping("/search")
    public Collection<BackgroundDto> findBackgrounds() {
        return backgroundService.getBackgrounds();
    }

    @PostMapping()
    public BackgroundDto addBackgrounds(@RequestBody final BackgroundDto backgroundDto) {
        return backgroundService.addBackground(backgroundDto);
    }

    @PutMapping("/{url}")
    public BackgroundDto updateBackgrounds(
            @PathVariable final String url,
            @RequestBody final BackgroundDto backgroundDto) {
        return backgroundService.updateBackgrounds(url, backgroundDto);
    }

    @DeleteMapping("/{url}")
    public BackgroundDto deleteBackgrounds(
            @PathVariable final String url) {
        return backgroundService.deleteBackgrounds(url);
    }
}
