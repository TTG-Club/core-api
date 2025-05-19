package club.ttg.dnd5.domain.beastiary.model;

import club.ttg.dnd5.domain.beastiary.model.action.BeastAction;
import club.ttg.dnd5.domain.beastiary.model.language.BeastLanguages;
import club.ttg.dnd5.domain.beastiary.model.section.BeastSection;
import club.ttg.dnd5.domain.beastiary.model.sense.BeastSenses;
import club.ttg.dnd5.domain.book.model.Book;
import club.ttg.dnd5.domain.common.dictionary.Alignment;
import club.ttg.dnd5.domain.common.dictionary.Condition;
import club.ttg.dnd5.domain.common.dictionary.DamageType;
import club.ttg.dnd5.domain.common.model.NamedEntity;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.util.Collection;

/**
 * Существо из бестиария
 */
@Getter
@Setter
@NoArgsConstructor

@Entity
@Table(name = "bestiary",
        indexes = {
                @Index(name = "url_index", columnList = "url"),
                @Index(name = "name_index", columnList = "name, english, alternative")
        }
)
public class Beast extends NamedEntity {
    /**
     * Размеры существа.
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Collection<BeastSize> sizes;

    /**
     * Типы существа.
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Collection<BeastCategory> categories;

    /**
     * Мирровозрение
     */
    @Enumerated(EnumType.STRING)
    private Alignment alignment;

    /**
     * Класс доспеха (КД)
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private BeastArmor armor;

    /**
     * Бонус инициативы
     */
    private byte initiative;

    /**
     * Хиты
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private BeastHit hit;

    /**
     * Скорости
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private BeastSpeeds speed;

    /**
     * Характеристики существа
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private BeastAbilities abilities;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Collection<BeastSkill> skills;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Collection<DamageType> vulnerabilities;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Collection<DamageType> resistance;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Collection<DamageType> immunityToDamage;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Collection<Condition> immunityToCondition;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Collection<BeastEquipment> equipments;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private BeastLanguages languages;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Collection<BeastSenses> senses;

    @Column(name = "exp")
    private Long experience;

    @Column(name = "exp_lair")
    private Long experienceInLair;

    @Column(name = "exp_suf")
    private String experienceSuffix;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Collection<BeastTrait> traits;

    /**
     * Действия
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Collection<BeastAction> actions;

    /**
     * Бонусные действия
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Collection<BeastAction> bonusActions;

    /**
     * Реакции
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Collection<BeastAction> reactions;

    /**
     * Количество легендарных действий
     */
    private byte legendaryAction;
    /**
     * Количество легендарных действий в логове
     */
    private byte legendaryActionInLair;
    /**
     * Легендарные действия
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Collection<BeastAction> legendaryActions;

    @ManyToOne
    @JoinColumn(name = "section_url")
    private BeastSection section;

    @ManyToOne
    @JoinColumn(name = "source")
    private Book source;
    private Long sourcePage;
}
