package club.ttg.dnd5.domain.tool.tracker.model;

import club.ttg.dnd5.domain.common.model.Timestamped;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.util.List;
import java.util.UUID;

/**
 * Участник трекера инициативы: игрок (имя + бонус вручную) или существо из бестиария.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "initiative_participant",
        indexes = {
                @Index(name = "initiative_participant_tracker_index", columnList = "tracker_id")
        })
public class InitiativeParticipant extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tracker_id", nullable = false)
    private UUID trackerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipantType type;

    @Column(nullable = false)
    private String name;

    /**
     * Бонус инициативы: игроку задаёт пользователь, существу — снапшот из бестиария
     * на момент добавления (правка бестиария не влияет на идущий бой).
     */
    @Column(name = "initiative_bonus", nullable = false)
    private int initiativeBonus;

    /**
     * Слаг существа в бестиарии. Ссылка мягкая, без FK — по конвенции проекта на bestiary
     * никто не ссылается; нужна фронту для перехода к статблоку. NULL — игрок.
     */
    @Column(name = "creature_url")
    private String creatureUrl;

    /**
     * Результат броска d20. NULL — инициатива ещё не брошена.
     */
    @Column(name = "initiative_roll")
    private Integer initiativeRoll;

    /**
     * Итог инициативы: бросок + бонус. NULL — инициатива ещё не брошена.
     */
    @Column(name = "initiative_total")
    private Integer initiativeTotal;

    /**
     * «Монетка» финального тай-брейка: случайное число, назначаемое при броске. Хранится,
     * чтобы порядок хода был детерминированным — участник, добавленный в идущий бой,
     * не пересортировывает остальных.
     */
    @Column(name = "tie_roll")
    private Integer tieRoll;

    /**
     * Порядковый номер добавления в трекер: стабильный порядок списка до броска
     * и последний тай-брейк после.
     */
    @Column(nullable = false)
    private int seq;

    /**
     * Повержен: участник остаётся в списке (виден, помечен мёртвым), но пропускается в порядке хода.
     * Снимается при завершении боя (reset).
     */
    @Column(nullable = false)
    private boolean dead;

    /**
     * Текущие хиты. NULL — мастер их не вёл: существо считается на полных (максимум статблока),
     * игрок — без хитов.
     */
    @Column(name = "current_hit_points")
    private Integer currentHitPoints;

    /**
     * Максимум хитов, заданный мастером (бросок формулы статблока или значение с листа).
     * NULL — берётся среднее из статблока, а у игрока хитов нет.
     */
    @Column(name = "max_hit_points")
    private Integer maxHitPoints;

    /**
     * Класс доспеха игрока: у существа он свой, из статблока, и здесь не хранится.
     * NULL — не задан.
     */
    @Column(name = "armor_class")
    private Integer armorClass;

    /**
     * Цвет иконки участника, когда картинки нет (ключ семантического цвета темы).
     * NULL — цвет по умолчанию.
     */
    @Column(name = "color", length = 20)
    private String color;

    /**
     * Привязка игрока к листу персонажа. NULL — игрок заведён вручную.
     */
    @Type(JsonType.class)
    @Column(name = "sheet_link", columnDefinition = "jsonb")
    private ParticipantSheetLink sheetLink;

    /**
     * Наложенные состояния. NULL или пустой список — состояний нет.
     */
    @Type(JsonType.class)
    @Column(name = "conditions", columnDefinition = "jsonb")
    private List<ParticipantCondition> conditions;
}
