package club.ttg.dnd5.service.character;

import club.ttg.dnd5.dto.page.PageResponse;
import club.ttg.dnd5.dto.spell.SpellDto;

public interface SpellService {
    SpellDto getSpell(String url);

    boolean existsByUrl(String url);

    PageResponse getSpells();
}
