package club.ttg.dnd5.service.character;

import club.ttg.dnd5.dto.page.PageResponse;
import club.ttg.dnd5.dto.spell.SpellDto;
import club.ttg.dnd5.exception.EntityNotFoundException;
import club.ttg.dnd5.model.spell.Spell;
import club.ttg.dnd5.repository.SpellRepository;
import club.ttg.dnd5.utills.Converter;
import club.ttg.dnd5.utills.converters.spell.SpellConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SpellServiceImpl implements SpellService {
    private final SpellRepository spellRepository;
    @Override
    public SpellDto getSpell(final String url) {
        return toDTO(findByUrl(url));
    }

    @Override
    public boolean existsByUrl(final String url) {
        return spellRepository.existsById(url);
    }

    @Override
    public PageResponse getSpells() {
        return null;
    }

    private SpellDto toDTO(final Spell spell) {
        return toDTO(spell, false);
    }

    private SpellDto toDTO(Spell spell, boolean hideDetails) {
        SpellDto dto = new SpellDto();
        if (hideDetails) {
            Converter.MAP_ENTITY_TO_BASE_DTO_WITH_HIDE_DETAILS.apply(dto, spell);
        } else {
            SpellConverter.MAP_ENTITY_TO_DTO_.apply(dto, spell);
            Converter.MAP_ENTITY_TO_BASE_DTO.apply(dto, spell);
        }
        return dto;
    }

    private Spell findByUrl(String url) {
        return spellRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException("Spell not found with URL: " + url));
    }
}
