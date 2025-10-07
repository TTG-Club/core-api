package club.ttg.dnd5.domain.spell.rest.dto.filter;

import club.ttg.dnd5.domain.character_class.rest.dto.ClassShortResponse;
import club.ttg.dnd5.dto.base.filters.AbstractFilterGroup;
import club.ttg.dnd5.dto.base.filters.AbstractFilterItem;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Фильтр заклинаний по подклассам (ManyToMany: spell.subclassAffiliation).
 * Положительные значения: включает заклинания, у которых среди подклассов есть любой из указанных.
 * Отрицательные значения: исключает заклинания, у которых есть любой из указанных.
 */
@Getter
@Setter
public class SpellSubclassFilterGroup extends AbstractFilterGroup<String, SpellSubclassFilterGroup.SpellSubclassFilterItem>
{
    public SpellSubclassFilterGroup(List<SpellSubclassFilterItem> filters)
    {
        super(filters);
    }

    @Override
    public String getName()
    {
        return "Подклассы";
    }

    @Override
    public BooleanExpression getQuery() {
        if (isSingular()) {
            return TRUE_EXPRESSION;
        }

        final Set<String> positives = getPositive();
        BooleanExpression result = CollectionUtils.isEmpty(positives)
                ? TRUE_EXPRESSION
                : Expressions.booleanTemplate(
                "exists (" +
                        "  select 1 " +
                        "  from spell_subclass_affiliation sca " +
                        "  where sca.spell_url = spell.url " +
                        "    and sca.subclass_affiliation_url = any (cast({0} as text[]))" +
                        ")",
                Expressions.constant(positives.toArray(String[]::new))
        );

        final Set<String> negatives = getNegative();
        return result.and(CollectionUtils.isEmpty(negatives)
                ? TRUE_EXPRESSION
                : Expressions.booleanTemplate(
                "not exists (" +
                        "  select 1 " +
                        "  from spell_subclass_affiliation sca " +
                        "  where sca.spell_url = spell.url " +
                        "    and sca.subclass_affiliation_url = any (cast({0} as text[]))" +
                        ")",
                Expressions.constant(negatives.toArray(String[]::new))
        ));
    }

    /**
     * Построение дефолтного набора пунктов фильтра из списка классов.
     */
    public static SpellSubclassFilterGroup getDefault(final List<ClassShortResponse> allClasses)
    {
        final List<SpellSubclassFilterItem> items = allClasses
                .stream()
                .filter(Objects::nonNull)
                .map(c -> new SpellSubclassFilterItem(
                        c.getName().getName(), c.getUrl())
                )
                .collect(Collectors.toList());

        return new SpellSubclassFilterGroup(items);
    }


    public static class SpellSubclassFilterItem extends AbstractFilterItem<String>
    {
        public SpellSubclassFilterItem(String label, String value)
        {
            super(label, value, null);
        }
    }
}
