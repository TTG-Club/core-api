package club.ttg.dnd5.domain.spell.service;

import club.ttg.dnd5.domain.spell.mapper.SpellMapper;
import club.ttg.dnd5.domain.spell.repository.SpellRepository;
import club.ttg.dnd5.domain.spell.rest.dto.SpellDetailedResponse;
import club.ttg.dnd5.domain.spell.rest.dto.SpellShortResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpellService {
    private final SpellRepository spellRepository;
    private final SpellMapper spellMapper;

    public List<SpellShortResponse> findAll() {
        return spellRepository.findAll().stream()
                .map(spellMapper::toSpeciesShortResponse)
                .collect(Collectors.toList());
    }

    public SpellDetailedResponse findByUrl(String url) {
        return spellRepository.findById(url)
                .map(spellMapper::toSpellDetailedResponse)
                .orElseThrow();
    }
}
