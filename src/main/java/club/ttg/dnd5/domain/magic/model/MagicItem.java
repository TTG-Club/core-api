package club.ttg.dnd5.domain.magic.model;

import club.ttg.dnd5.dictionary.item.magic.Rarity;
import club.ttg.dnd5.domain.common.model.NamedEntity;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "item-magic", indexes = {
        @Index(name = "url_index", columnList = "url"),
        @Index(name = "name_index", columnList = "name, english, alternative")
})
public class MagicItem extends NamedEntity {
    /**
     * Категория магического предмета.
     */
    @Enumerated(EnumType.STRING)
    private MagicItemCategory category;
    /**
     * Уточнение типа магического предмета, например (любой меч)
     */
    private String clarification;
    /**
     * Редкость (только для магических предметов).
     */
    @Enumerated(EnumType.STRING)
    private Rarity rarity;

    /**
     * Настройка на магический предмет
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Attunement attunement;

    /**
     * Количество зарядов магического предмета.
     */
    private Short charges;
    /**
     * True если предмет проклят.
     */
    private boolean curse;
}
