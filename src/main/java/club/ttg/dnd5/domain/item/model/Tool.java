package club.ttg.dnd5.domain.item.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

/**
 * Инструмент
 */
@Getter
@Setter
@Entity
@DiscriminatorValue("TOOL")
public class Tool extends Item {
    /**
     * Хапактеристика
     */
    public String ability;
    /**
     * Использование
     */
    public String uses;
    /**
     * Создание
     */
    public String creation;
}
