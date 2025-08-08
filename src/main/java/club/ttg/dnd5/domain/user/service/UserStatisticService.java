package club.ttg.dnd5.domain.user.service;

import club.ttg.dnd5.domain.background.repository.BackgroundRepository;
import club.ttg.dnd5.domain.beastiary.repository.CreatureRepository;
import club.ttg.dnd5.domain.feat.repository.FeatRepository;
import club.ttg.dnd5.domain.common.model.SectionType;
import club.ttg.dnd5.domain.glossary.repository.GlossaryRepository;
import club.ttg.dnd5.domain.item.repository.ItemRepository;
import club.ttg.dnd5.domain.magic.repository.MagicItemRepository;
import club.ttg.dnd5.domain.species.repository.SpeciesRepository;
import club.ttg.dnd5.domain.spell.repository.SpellRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserStatisticService {
    private final SpellRepository spellRepository;
    private final GlossaryRepository glossaryRepository;
    private final CreatureRepository bestiaryRepository;
    private final MagicItemRepository magicItemRepository;
    private final SpeciesRepository speciesRepository;
    private final FeatRepository featRepository;
    private final BackgroundRepository backgroundRepository;
    private final ItemRepository itemRepository;

    public Integer getUserStatistics(String username, SectionType type) {
        return switch (type) {
            case SPELL -> spellRepository.countByUsername(username);
            case GLOSSARY -> glossaryRepository.countByUsername(username);
            case BESTIARY -> bestiaryRepository.countByUsername(username);
            case MAGIC_ITEM -> magicItemRepository.countByUsername(username);
            case SPECIES -> speciesRepository.countByUsername(username);
            case FEAT -> featRepository.countByUsername(username);
            case BACKGROUND -> backgroundRepository.countByUsername(username);
            case ITEM -> itemRepository.countByUsername(username);
        };
    }
}
