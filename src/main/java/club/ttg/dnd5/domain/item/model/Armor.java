package club.ttg.dnd5.domain.item.model;

import club.ttg.dnd5.domain.common.dictionary.ArmorCategory;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@Entity
@DiscriminatorValue("ARMOR")
public class Armor extends Item {
    @Enumerated(EnumType.STRING)
    private ArmorCategory category;
    /** КД. */
    private String armorClass;
    /** Сила. */
    private String strength;
    /** Скрытность. */
    private boolean stealth;
}
