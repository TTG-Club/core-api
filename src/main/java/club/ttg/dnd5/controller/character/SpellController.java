package club.ttg.dnd5.controller.character;

import club.ttg.dnd5.dto.page.PageRequest;
import club.ttg.dnd5.dto.page.PageResponse;
import club.ttg.dnd5.dto.spell.SpellDto;
import club.ttg.dnd5.service.character.SpellService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Заклинания", description = "REST API заклинаний")

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/spell")
public class SpellController {
    private final SpellService spellService;

    /**
     * Проверка существования заклинания по URL.
     *
     * @param url URL заклинания.
     * @return 204, если заклинание с таким URL не существует; 409, если существует.
     */
    @Operation(
            summary = "Проверка существования заклинания",
            description = "Возвращает 204 (No Content), если вида с указанным URL не существует, или 409 (Conflict), если заклинание существует."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Заклинание с указанным URL не найден."),
            @ApiResponse(responseCode = "409", description = "Заклинание с указанным URL уже существует.")
    })
    @RequestMapping(value = "/{url}", method = RequestMethod.HEAD)
    public ResponseEntity<Void> handleOptions(@PathVariable("url") String url) {
        boolean exists = spellService.existsByUrl(url);
        if (exists) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } else {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
    }

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
    @Operation(summary = "Получение списка краткого описания черты")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Черты успешно получена")
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/search")
    public PageResponse getSpells(@RequestBody PageRequest request) {
        return spellService.getSpells();
    }
}
