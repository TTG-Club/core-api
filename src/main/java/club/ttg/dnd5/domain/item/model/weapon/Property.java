package club.ttg.dnd5.domain.item.model.weapon;

import club.ttg.dnd5.domain.common.model.Roll;
import club.ttg.dnd5.domain.item.rest.dto.Range;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Property {
    private String url;
    private String name;
    private Range range;
    private Roll versatile;
    /**
     * Требуемый тип снаряда для выстрела (только дальнобойного)
     */
    private AmmunitionType ammo;
    private String additional;

    public String toString() {
        var builder = new StringBuilder(name);
        if (range != null) {
            builder.append("(дистанция ");
            builder.append(range.getNormal());
            builder.append("/");
            builder.append(range.getMax());
            if (ammo != null) {
                builder.append(", ");
                builder.append(ammo.getName());
            }
            builder.append(")");
        }
        if (versatile != null) {
            builder.append("(");
            builder.append(versatile);
            builder.append(")");
        }
        return builder.toString();
    }
}
