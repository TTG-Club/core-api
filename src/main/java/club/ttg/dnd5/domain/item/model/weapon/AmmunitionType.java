package club.ttg.dnd5.domain.item.model.weapon;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AmmunitionType {
    ARROW("Стрела"),
    BOLT("Болт"),
    SLING_BULLET("Игла для трубки"),
    BULLET("Пуля");

    private final String name;
}
