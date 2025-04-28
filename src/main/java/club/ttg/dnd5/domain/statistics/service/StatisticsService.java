package club.ttg.dnd5.domain.statistics.service;

import club.ttg.dnd5.domain.feat.model.QFeat;
import club.ttg.dnd5.domain.species.model.QSpecies;
import club.ttg.dnd5.domain.spell.model.QSpell;
import com.querydsl.core.types.dsl.EntityPathBase;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsService {
    private final EntityManager entityManager;
    private final List<EntityPathBase<?>> COUNTED_ENTITIES = List.of(QSpell.spell, QSpecies.species, QFeat.feat);


    @Cacheable(cacheNames = "countAllMaterials")
    public Long countAllMaterials() {
        return COUNTED_ENTITIES.stream().map(
                        e -> entityManager.createNativeQuery(String.format("SELECT COUNT(*) FROM %s WHERE is_hidden_entity = false", e.toString()), Long.class)
                                .getSingleResult())
                .mapToLong(e -> (Long)e)
                .sum();
    }
}

