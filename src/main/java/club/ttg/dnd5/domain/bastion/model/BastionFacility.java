package club.ttg.dnd5.domain.bastion.model;

import club.ttg.dnd5.domain.common.model.NamedEntity;
import club.ttg.dnd5.domain.source.model.Source;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.util.List;

/**
 * Сооружение бастиона — базовое или специализированное.
 *
 * <p>Кроме текста для справочника запись несёт разобранные правила: пространство, наёмники,
 * приказы с вариантами, цены и сроки, расширение, выборы. На них будет опираться мини-игра
 * «Бастион», поэтому текст описания не должен быть единственным местом, где живут числа.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "bastion_facility",
        indexes = {
                @Index(name = "bastion_facility_name_index", columnList = "name, english, alternative")
        }
)
public class BastionFacility extends NamedEntity {
    @Enumerated(EnumType.STRING)
    private FacilityCategory category;

    /** Требуемый уровень персонажа; у базовых сооружений пуст. */
    private Long level;

    /** Требование помимо уровня; {@code null} — требований нет. */
    @Enumerated(EnumType.STRING)
    private FacilityPrerequisite prerequisite;

    /** Пространство, в котором сооружение появляется в бастионе. */
    @Enumerated(EnumType.STRING)
    private FacilitySpace space;

    /** Число наёмников, с которыми появляется сооружение. */
    private Integer hirelings;

    /** Можно ли иметь в бастионе больше одного такого сооружения. */
    private Boolean repeatable;

    /** Приказы, которые принимает сооружение. */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private List<BastionOrder> orders;

    /** Варианты исполнения приказов. */
    @Type(JsonType.class)
    @Column(name = "order_options", columnDefinition = "jsonb")
    private List<FacilityOrderOption> orderOptions;

    /** Расширение сооружения; {@code null} — не расширяется. */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private FacilityEnlargement enlargement;

    /** Постоянные свойства сооружения. */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private List<FacilityFeature> features;

    /** Выборы, которые игрок делает для своего сооружения. */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private List<FacilityChoice> choices;

    @ManyToOne
    @JoinColumn(name = "source")
    private Source source;
    private Long sourcePage;
}
