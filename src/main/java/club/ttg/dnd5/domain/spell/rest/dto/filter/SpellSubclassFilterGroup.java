package club.ttg.dnd5.domain.spell.rest.dto.filter;

import club.ttg.dnd5.domain.character_class.model.CharacterClass;
import club.ttg.dnd5.dto.base.filters.AbstractFilterGroup;
import club.ttg.dnd5.dto.base.filters.AbstractFilterItem;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Comparator;
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
@JsonTypeName("s-sub")
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
                """
                        exists (\
                          select 1 \
                          from spell_subclass_affiliation ssa \
                          where ssa.spell_url = spell.url \
                            and ssa.subclass_affiliation_url = any (cast({0} as text[]))\
                        )
                        """,
                Expressions.constant(positives.toArray(String[]::new))
        );

        final Set<String> negatives = getNegative();
        return result.and(CollectionUtils.isEmpty(negatives)
                ? TRUE_EXPRESSION
                : Expressions.booleanTemplate(
                """
                        not exists (\
                          select 1 \
                          from spell_subclass_affiliation ssa \
                          where ssa.spell_url = spell.url \
                            and ssa.subclass_affiliation_url = any (cast({0} as text[]))\
                        )
                        """,
                Expressions.constant(negatives.toArray(String[]::new))
        ));
    }

    /**
     * Построение дефолтного набора пунктов фильтра из списка классов.
     */
    public static SpellSubclassFilterGroup getDefault(final List<CharacterClass> allClasses)
    {
        final List<SpellSubclassFilterItem> items = allClasses
                .stream()
                .filter(Objects::nonNull)
                .map(c -> new SpellSubclassFilterItem(
                        c.getName(), c.getUrl())
                )
                .sorted(Comparator.comparing(AbstractFilterItem::getName))
                .collect(Collectors.toList());

        return new SpellSubclassFilterGroup(items);
    }

    @JsonTypeName("s-sub-i")
    public static class SpellSubclassFilterItem extends AbstractFilterItem<String>
    {
        public SpellSubclassFilterItem(String label, String value)
        {
            super(label, value, null);
        }
    }
}
