package club.ttg.dnd5.domain.item.model.weapon;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AmmunitionType {
    ARROW("стрела"),
    BOLT("болт"),
    SLING_BULLET("игла для трубки"),
    BULLET("пуля");

    private final String name;
}
