package club.ttg.dnd5.domain.background.model;

import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.Skill;
import club.ttg.dnd5.domain.common.model.ActiveEffect;
import club.ttg.dnd5.domain.common.model.EntityRef;
import club.ttg.dnd5.domain.common.model.EquipmentOption;
import club.ttg.dnd5.domain.common.model.NamedEntity;
import club.ttg.dnd5.domain.feat.model.Feat;
import club.ttg.dnd5.domain.feat.model.mechanics.FeatMechanics;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor

@Entity
@Table(name = "background",
        indexes = {
                @Index(name = "url_index", columnList = "url"),
                @Index(name = "name_index", columnList = "name, english, alternative")
        }
)
public class Background extends NamedEntity {
    private String linkImageUrl; //для изоброжения бэкграунда
    /** Характеристики */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Set<Ability> abilities;

    /** Доступные умения. */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Set<Skill> skillProficiencies;

    /** Черта */
    @ManyToOne
    @JoinColumn(name = "feat_id")
    private Feat feat;

    /**
     * Черты на выбор, когда предыстория не называет одну: игрок берёт любую из списка.
     *
     * <p>Ссылками, а не связью: список правится в мастерской одним полем формы, читается
     * без join'а и хранит снимок названия — как остальные ссылки JSONB-полей
     * ({@link EntityRef}). Фиксированная черта живёт в {@link #feat}: она участвует в
     * фильтрах и в обратной выборке «предыстории, дающие эту черту», а вариант выбора —
     * нет.</p>
     */
    @Type(JsonType.class)
    @Column(name = "feat_choices", columnDefinition = "jsonb")
    private List<EntityRef> featChoices;

    /**
     * Уточнение черты (например для просвещенный в магию)
     */
    private String featSuffix;

    /** Владение инструментами */
    private String toolProficiency;

    /**
     * Владение инструментами ссылками на записи раздела «Предметы».
     *
     * <p>Рядом со свободным текстом {@link #toolProficiency}, а не вместо него: текст
     * остаётся у записей, которые на структуру ещё не перевели, и продолжает показываться
     * на странице. Заполненные ссылки главнее — по ним лист персонажа выдаёт владение, а
     * выгрузка переводит адрес страницы в ключ вокабуляра стола.</p>
     */
    @Type(JsonType.class)
    @Column(name = "tool_proficiencies", columnDefinition = "jsonb")
    private List<EntityRef> toolProficiencies;

    /** Владение инструментами на выбор игрока; {@code null} — выбора нет. */
    @Type(JsonType.class)
    @Column(name = "tool_choice", columnDefinition = "jsonb")
    private BackgroundToolChoice toolChoice;

    /**
     * Расширенные дары предыстории: владения, языки, защиты, чувства, выборы игрока и
     * выдаваемые заклинания — то, что лист персонажа проставляет сам.
     *
     * <p>Моделью черты, а не своей: набор даров у предыстории тот же, и система хранит их
     * в том же блобе, что у черты ({@code featData}). Второй набор классов означал бы
     * второй маппинг в компендиум для тех же самых полей. Тот же приём, что у вида
     * ({@code SpeciesMechanics} повторяет блоки {@code FeatMechanics}), только здесь
     * повторять нечего — модель переиспользуется целиком.</p>
     *
     * <p>Канонические дары предыстории (характеристики, два навыка, инструменты, черта)
     * сюда НЕ дублируются: они лежат своими полями, и мастер настройки читает их оттуда.
     * Здесь — только то, чего в канонических полях нет.</p>
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private FeatMechanics mechanics;

    /**
     * Активные эффекты предыстории в вокабуляре VTTG — та же модель и та же отдельная
     * колонка, что у черты ({@code Feat.activeEffects}) и предмета.
     */
    @Type(JsonType.class)
    @Column(name = "active_effects", columnDefinition = "jsonb")
    private List<ActiveEffect> activeEffects;

    /** Снаряжение */
    private String equipment;

    /**
     * Стартовое снаряжение вариантами выбора: «А» — предметы, «Б» — монеты и т.д.
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb", name = "starting_equipment")
    private List<EquipmentOption> startingEquipment;
    /** Предлагаемый класс */
    private String proposeClasses;

    @ManyToOne
    @JoinColumn(name = "source")
    private Source source;
    private Long sourcePage;
}
