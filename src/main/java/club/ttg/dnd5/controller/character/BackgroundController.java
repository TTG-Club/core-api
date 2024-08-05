package club.ttg.dnd5.controller.character;

import club.ttg.dnd5.dto.character.BackgroundRequest;
import club.ttg.dnd5.service.character.BackgroundService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@Tag(name = "Предыстории", description = "REST API предысторий персонажа")

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/background")
public class BackgroundController {
    private final BackgroundService backgroundService;
    @GetMapping
    public Collection<BackgroundRequest> getBackgrounds() {
        return null;
    }
}
