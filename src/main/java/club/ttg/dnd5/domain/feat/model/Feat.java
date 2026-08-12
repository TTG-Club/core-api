package club.ttg.dnd5.domain.feat.model;

import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.model.NamedEntity;
import club.ttg.dnd5.domain.feat.model.mechanics.FeatMechanics;
import club.ttg.dnd5.domain.feat.model.prerequisite.FeatPrerequisite;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.util.Collection;

/**
 * Черты.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "feat",
        indexes = {
                @Index(name = "url_index", columnList = "url"),
                @Index(name = "name_index", columnList = "name, english, alternative")
        }
)
public class Feat extends NamedEntity {
    /**
     * Категория.
     */
    @Enumerated(EnumType.STRING)
    private FeatCategory category;
    /**
     * Улучшаемые характеристики — плоская проекция
     * {@code mechanics.abilityBonuses[*].abilities} для SQL-фильтра «Характеристика»
     * (см. {@code FeatPredicateBuilder}). Не редактируется руками: пересобирается
     * маппером при сохранении черты.
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Collection<Ability> abilities;

    /**
     * Механика влияния черты на лист персонажа.
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private FeatMechanics mechanics;

    /**
     * Предварительное условие, как оно напечатано в книге. Показывается игроку;
     * для проверок служит {@link #prerequisiteDetails}.
     */
    private String prerequisite;

    /**
     * Предварительное условие в разобранном виде.
     */
    @Type(JsonType.class)
    @Column(name = "prerequisite_details", columnDefinition = "jsonb")
    private FeatPrerequisite prerequisiteDetails;
    /**
     * Можно брать черту больше чем один раз
     */
    private Boolean repeatability;

    @ManyToOne
    @JoinColumn(name = "source")
    private Source source;
    private Long sourcePage;
}
