package club.ttg.dnd5.domain.statistics.service;

import club.ttg.dnd5.domain.background.model.Background;
import club.ttg.dnd5.domain.beastiary.model.Creature;
import club.ttg.dnd5.domain.feat.model.Feat;
import club.ttg.dnd5.domain.glossary.model.Glossary;
import club.ttg.dnd5.domain.item.model.Item;
import club.ttg.dnd5.domain.magic.model.MagicItem;
import club.ttg.dnd5.domain.species.model.Species;
import club.ttg.dnd5.domain.spell.model.Spell;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Table;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {
    private final EntityManager entityManager;
    private final List<Class<?>> COUNTED_ENTITIES = List.of(
            Spell.class,
            Species.class,
            Feat.class,
            Background.class,
            MagicItem.class,
            Item.class,
            Glossary.class,
            Creature.class
    );
    private final Set<String> COUNTED_ENTITIES_TABLE_NAMES = COUNTED_ENTITIES.stream().map(clazz -> clazz.getAnnotation(Table.class).name()).collect(Collectors.toSet());

    @Cacheable(cacheNames = "countAllMaterials")
    public Long countAllMaterials() {
        return COUNTED_ENTITIES_TABLE_NAMES.stream().map(
                        tableName -> entityManager.createNativeQuery(
                                "SELECT COUNT(*) FROM %s WHERE is_hidden_entity = false".format(tableName), Long.class)
                                .getSingleResult())
                .mapToLong(e -> (Long)e)
                .sum();
    }
}

