package club.ttg.dnd5.domain.spell.rest.dto.filter;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Фильтр заклинаний по подклассам (ManyToMany: spell.classAffiliation).
 * Положительные значения: включает заклинания, у которых среди классов есть любой из указанных.
 * Отрицательные значения: исключает заклинания, у которых есть любой из указанных.
 */
@Getter
@Setter
public class SpellSubclassFilterGroup extends SpellClassFilterGroup
{
    public SpellSubclassFilterGroup(final List<SpellClassFilterItem> filters) {
        super(filters);
    }

    @Override
    public String getName()
    {
        return "Подклассы";
    }
}
