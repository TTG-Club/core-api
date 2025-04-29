package club.ttg.dnd5.domain.spell.rest.dto.filter;

import club.ttg.dnd5.domain.spell.model.QSpell;
import club.ttg.dnd5.dto.base.filters.AbstractFilterGroup;
import club.ttg.dnd5.dto.base.filters.AbstractFilterItem;
import com.querydsl.core.types.dsl.SimpleExpression;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Getter
@Setter
public class SpellLevelFilterGroup extends AbstractFilterGroup<Long, SpellLevelFilterGroup.SpellLevelFilterItem> {


    public SpellLevelFilterGroup(List<SpellLevelFilterItem> filters) {
        super(filters);
    }

    @Override
    public String getName() {
        return "Заклинания";
    }

    @Override
    protected SimpleExpression<Long> getPATH() {
        return QSpell.spell.level;
    }

    public static SpellLevelFilterGroup getDefault() {
        return new SpellLevelFilterGroup(LongStream.range(0, 10).boxed()
                .map(i -> new SpellLevelFilterItem(i == 0L ? "заговор" : i.toString(), i))
                .collect(Collectors.toList()));
    }

    public static class SpellLevelFilterItem extends AbstractFilterItem<Long> {
        public SpellLevelFilterItem(String name, Long value) {
            super(name, value, null);
        }
    }


}
