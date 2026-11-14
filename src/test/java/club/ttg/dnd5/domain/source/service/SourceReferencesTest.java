package club.ttg.dnd5.domain.source.service;

import club.ttg.dnd5.domain.background.model.Background;
import club.ttg.dnd5.domain.bastion.model.BastionFacility;
import club.ttg.dnd5.domain.beastiary.model.Creature;
import club.ttg.dnd5.domain.character_class.model.CharacterClass;
import club.ttg.dnd5.domain.feat.model.Feat;
import club.ttg.dnd5.domain.glossary.model.Glossary;
import club.ttg.dnd5.domain.item.model.Item;
import club.ttg.dnd5.domain.magic.model.MagicItem;
import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.species.model.Species;
import club.ttg.dnd5.domain.spell.model.Spell;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SourceReferencesTest {

    /** Все разделы каталога, ссылающиеся на источник, находятся по сущностям. */
    @Test
    void findsEverySectionTable() {
        Set<SourceReferences.Reference> references = SourceReferences.of(List.of(
                Spell.class, Creature.class, CharacterClass.class, Species.class, Feat.class,
                Background.class, Item.class, MagicItem.class, Glossary.class, BastionFacility.class,
                Source.class));

        assertEquals(
                Set.of("spell", "bestiary", "class", "species", "feat", "background", "item", "magic_item",
                        "glossary", "bastion_facility"),
                references.stream().map(SourceReferences.Reference::table).collect(Collectors.toSet()));
        assertEquals(Set.of("source"),
                references.stream().map(SourceReferences.Reference::column).collect(Collectors.toSet()));
    }
}
