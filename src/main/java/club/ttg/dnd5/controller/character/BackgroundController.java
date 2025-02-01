package club.ttg.dnd5.controller.character;

import club.ttg.dnd5.dto.character.BackgroundDto;
import club.ttg.dnd5.exception.EntityExistException;
import club.ttg.dnd5.service.character.BackgroundService;
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

    @GetMapping("{backgroundUrl}")
    public BackgroundDto findBackground(@PathVariable final String backgroundUrl) {
        return backgroundService.getBackground(backgroundUrl);
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(path = "/{backgroundUrl}", method = RequestMethod.OPTIONS)
    public ResponseEntity<Boolean> existByUrl(@PathVariable final String backgroundUrl) {
        if (backgroundService.existByUrl(backgroundUrl)) {
            throw new EntityExistException();
        }
        return ResponseEntity.ok(false);
    }

    @PostMapping("/search")
    public Collection<BackgroundDto> findBackgrounds() {
        return backgroundService.getBackgrounds();
    }

    @PostMapping()
    public BackgroundDto addBackgrounds(@RequestBody final BackgroundDto backgroundDto) {
        return backgroundService.addBackground(backgroundDto);
    }

    @PutMapping("{backgroundUrl}")
    public BackgroundDto updateBackgrounds(
            @PathVariable final String backgroundUrl,
            @RequestBody final BackgroundDto backgroundDto) {
        return backgroundService.updateBackgrounds(backgroundUrl, backgroundDto);
    }

    @DeleteMapping("{backgroundUrl}")
    public BackgroundDto deleteBackgrounds(
            @PathVariable final String backgroundUrl) {
        return backgroundService.deleteBackgrounds(backgroundUrl);
    }
}
