package club.ttg.dnd5.domain.spell.rest.controller;

import club.ttg.dnd5.domain.spell.rest.dto.SpellDetailedResponse;
import club.ttg.dnd5.domain.spell.rest.dto.SpellShortResponse;
import club.ttg.dnd5.domain.spell.rest.dto.create.SpellRequest;
import club.ttg.dnd5.domain.spell.service.SpellService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Заклинания", description = "REST API заклинаний")

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/spells")
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

    @Secured("ADMIN")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SpellDetailedResponse createSpell(@RequestBody SpellRequest request) {
        return spellService.save(request);
    }

}
