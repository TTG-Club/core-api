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
 * Фильтр заклинаний по Классам (ManyToMany: spell.classAffiliation).
 * Положительные значения: включает заклинания, у которых среди классов есть любой из указанных.
 * Отрицательные значения: исключает заклинания, у которых есть любой из указанных.
 */
@Getter
@Setter
public class SpellClassFilterGroup extends AbstractFilterGroup<String, SpellClassFilterGroup.SpellClassFilterItem>
{
    public SpellClassFilterGroup(List<SpellClassFilterItem> filters)
    {
        super(filters);
    }

    @Override
    public String getName()
    {
        return "Классы";
    }

    @Override
    public BooleanExpression getQuery()
    {
        if (isSingular())
        {
            return TRUE_EXPRESSION;
        }

        final Set<String> positives = getPositive();
        BooleanExpression result = CollectionUtils.isEmpty(positives)
                ? TRUE_EXPRESSION
                : Expressions.booleanTemplate(
                """
                        exists ( \
                        select 1
                         from spell_class_affiliation sca
                                  join class cc on cc.url = sca.class_affiliation_url
                         where cc.url = any ({0}::text[])
                        )""",
                Expressions.constant(positives.toArray(String[]::new))
        );

        final Set<String> negatives = getNegative();
        return result.and(CollectionUtils.isEmpty(negatives)
                ? TRUE_EXPRESSION
                : Expressions.booleanTemplate(
                """
                        not exists ( \
                          select 1 \
                          from spell_class_affiliation sca \
                          join class cc on cc.id = sca.class_affiliation_id \
                          where sca.spell_id = spell.id \
                            and cc.url = any ({0}::text[])
                        )""",
                Expressions.constant(negatives.toArray(String[]::new))
        ));
    }

    /**
     * Построение дефолтного набора пунктов фильтра из списка классов.
     */
    public static SpellClassFilterGroup getDefault(final List<ClassShortResponse> allClasses)
    {
        final List<SpellClassFilterItem> items = allClasses
                .stream()
                .filter(Objects::nonNull)
                .map(c -> new SpellClassFilterItem(
                        c.getName().getName(), c.getUrl())
                )
                .collect(Collectors.toList());

        return new SpellClassFilterGroup(items);
    }


    public static class SpellClassFilterItem extends AbstractFilterItem<String>
    {
        public SpellClassFilterItem(String label, String value)
        {
            super(label, value, null);
        }
    }
}
