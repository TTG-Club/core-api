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
    private CreatureSize sizes;

    /**
     * Типы существа.
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private CreatureCategory types;

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
    private CreatureSpeeds speeds;

    /**
     * Характеристики существа
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private CreatureAbilities abilities;

    /**
     * Навыки
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Collection<CreatureSkill> skills;

    /**
     * Уязвимости
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Collection<DamageType> vulnerabilities;

    /**
     * Сопротивления
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Collection<DamageType> resistance;

    /**
     * Иммунитеты к урону
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Collection<DamageType> immunityToDamage;

    /**
     * Иммунитеты к состояниям
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Collection<Condition> immunityToCondition;

    /**
     * Снаряжение
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Collection<CreatureEquipment> equipments;

    /**
     * Языки
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private CreatureLanguages languages;

    /**
     * Чувства
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Senses senses;

    /**
     * Опыт
     */
    private Long experience;

    /**
     * Опыт в логове
     */
    private Long experienceInLair;

    /**
     * Дополнительный текст в ПО
     */
    private String experienceSuffix;

    /**
     * Черты
     */
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
    /**
     * Секция
     */
    @ManyToOne
    @JoinColumn(name = "section_url")
    private CreatureSection section;

    @ManyToOne
    @JoinColumn(name = "source")
    private Book source;
    private Long sourcePage;
}
