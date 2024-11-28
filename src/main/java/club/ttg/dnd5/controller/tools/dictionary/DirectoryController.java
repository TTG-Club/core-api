package club.ttg.dnd5.controller.tools.dictionary;

import club.ttg.dnd5.dto.base.ValueDto;
import club.ttg.dnd5.service.base.DirectoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

/**
 * Различные справочники
 */
@RequiredArgsConstructor
@Tag(name = "Справочники", description = "API для различных справочников")
@RequestMapping("/api/v2/directory/")
@RestController
public class DirectoryController {
    private final DirectoryService directoryService;

    @Operation(summary = "Дайсы")
    @GetMapping("/dices")
    public Collection<ValueDto> getDices() {
        return directoryService.getDices();
    }

    @Operation(summary = "Типы существ")
    @GetMapping("/beast/types")
    public Collection<ValueDto> getCreatureCategories() {
        return directoryService.getCreatureCategories();
    }

    @Operation(summary = "Размеры существ")
    @GetMapping("/sizes")
    public Collection<ValueDto> getCreatureSizes() {
        return directoryService.getCreatureSizes();
    }

    @Operation(summary = "Типы урона")
    @GetMapping("/damage/types")
    public Collection<ValueDto> getDamageTypes() {
        return directoryService.getDamageTypes();
    }

    @Operation(summary = "Состояния")
    @GetMapping("/conditions")
    public Collection<ValueDto> getConditions() {
        return directoryService.getConditions();
    }

    @Operation(summary = "Мировоззрение")
    @GetMapping("/alignments")
    public Collection<ValueDto> getAlignments() {
        return directoryService.getAlignments();
    }

    @Operation(summary = "Места обитания существ")
    @GetMapping("/environments")
    public Collection<ValueDto> getEnvironments() {
        return directoryService.getEnvironments();
    }

    @Operation(summary = "Типы черт")
    @GetMapping("/type/feats") // Здесь замена '_' на '/'
    public Collection<ValueDto> getFeatTypes() {
        return directoryService.getFeatTypes();
    }

    @Operation(summary = "Типы заклинателей")
    @GetMapping("/spellcaster/types") // Здесь замена '_' на '/'
    public Collection<ValueDto> getSpellcasterTypes() {
        return directoryService.getSpellcasterTypes();
    }

    @Operation(summary = "Типы черт")
    @GetMapping("/feat/types") // Здесь замена '_' на '/'
    public Collection<ValueDto> getFeatTypesSpellcasterTypes() {
        return directoryService.getFeatTypesSpellcasterTypes();
    }
}
