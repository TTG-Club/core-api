package club.ttg.dnd5.controller.character;

import club.ttg.dnd5.dto.spell.SpellDto;
import club.ttg.dnd5.service.character.SpellService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Заклинания", description = "REST API заклинаний")

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/spell")
public class SpellController {
    private final SpellService spellService;

    @Operation(summary = "Получение детального описания заклинания]")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Заклинание успешно получена"),
            @ApiResponse(responseCode = "404", description = "Заклинание не найдено")
    })

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{url}")
    public SpellDto getSpell(@PathVariable final String url) {
        return spellService.getSpell(url);
    }
}
