package club.ttg.dnd5.domain.item.model;

import club.ttg.dnd5.dictionary.item.magic.Rarity;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@Entity
@DiscriminatorValue("MAGIC_ITEM")
public class MagicItem extends Item {
    /**
     * Уточнение типа магического предмета, например (любой меч)
     */
    private String typeClarification;
    /**
     * Редкость (только для магических предметов).
     */
    @Enumerated(EnumType.STRING)
    private Rarity rarity;
    /**
     * True если требуется настройка на магический предмет или текст для выборочной настройки.
     */
    private String attunement;
    /**
     * Количество зарядов магического предмета.
     */
    private Byte charges;
    /**
     * True если предмет проклят.
     */
    private boolean curse;
}
