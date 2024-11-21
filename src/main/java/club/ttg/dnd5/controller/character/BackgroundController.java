package club.ttg.dnd5.controller.character;

import club.ttg.dnd5.dto.character.BackgroundDto;
import club.ttg.dnd5.service.character.BackgroundService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v2/background")
public class BackgroundController {
    private final BackgroundService backgroundService;

    @GetMapping("{backgroundUrl}")
    public BackgroundDto findBackground(@PathVariable final String backgroundUrl) {
        return backgroundService.getBackground(backgroundUrl);
    }
}
