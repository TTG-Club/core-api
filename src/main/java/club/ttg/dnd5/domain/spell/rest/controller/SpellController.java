package club.ttg.dnd5.domain.spell.rest.controller;

import club.ttg.dnd5.domain.spell.rest.dto.SpellDetailedResponse;
import club.ttg.dnd5.domain.spell.rest.dto.SpellShortResponse;
import club.ttg.dnd5.domain.spell.service.SpellService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Заклинания", description = "REST API заклинаний")

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/spell")
public class SpellController {
    private final SpellService spellService;

    @PostMapping("/search")
    public List<SpellShortResponse> getSpells() {
        return spellService.findAll();
    }

    @GetMapping("/{url}")
    public SpellDetailedResponse getSpellsByUrl(@PathVariable String url) {
        return spellService.findByUrl(url);
    }
}
