package club.ttg.dnd5.controller.character;

import club.ttg.dnd5.dto.character.SpellRequest;
import club.ttg.dnd5.service.character.SpellService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@Tag(name = "Заклинания", description = "REST API заклинаний")

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/spell")
public class SpellController {
    private final SpellService spellService;
    @GetMapping
    public Collection<SpellRequest> getSpells() {
        return null;
    }
}
