package club.ttg.dnd5.domain.beastiary.model;

import club.ttg.dnd5.domain.beastiary.model.action.CreatureAction;
import club.ttg.dnd5.domain.beastiary.model.language.CreatureLanguages;
import club.ttg.dnd5.domain.beastiary.model.section.CreatureSection;
import club.ttg.dnd5.domain.beastiary.model.sense.Senses;
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
public class Creature extends NamedEntity {
    /**
     * Размеры существа.
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Collection<CreatureSize> sizes;

    /**
     * Типы существа.
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private BeastCategory categories;

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
    private CreatureArmor armor;

    /**
     * Бонус инициативы
     */
    private byte initiative;

    /**
     * Хиты
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private CreatureHit hit;

    /**
     * Скорости
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private CreatureSpeeds speed;

    /**
     * Характеристики существа
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private BeastAbilities abilities;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Collection<CreatureSkill> skills;

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
    private Collection<CreatureEquipment> equipments;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private CreatureLanguages languages;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Senses senses;

    @Column(name = "exp")
    private Long experience;

    @Column(name = "exp_lair")
    private Long experienceInLair;

    @Column(name = "exp_suf")
    private String experienceSuffix;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Collection<CreatureTrait> traits;

    /**
     * Действия
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Collection<CreatureAction> actions;

    /**
     * Бонусные действия
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Collection<CreatureAction> bonusActions;

    /**
     * Реакции
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Collection<CreatureAction> reactions;

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
    private Collection<CreatureAction> legendaryActions;

    @ManyToOne
    @JoinColumn(name = "section_url")
    private CreatureSection section;

    @ManyToOne
    @JoinColumn(name = "source")
    private Book source;
    private Long sourcePage;
}
