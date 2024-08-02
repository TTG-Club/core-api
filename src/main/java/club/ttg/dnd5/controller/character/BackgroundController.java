package club.ttg.dnd5.controller.character;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Предыстории", description = "REST API предысторий персонажа")

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/background")
public class BackgroundController {
}
