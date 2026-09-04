package club.ttg.dnd5.domain.character_class.service;

import club.ttg.dnd5.domain.character_class.model.QCharacterClass;
import club.ttg.dnd5.domain.character_class.rest.dto.ClassQueryRequest;
import club.ttg.dnd5.domain.common.dictionary.Dice;
import club.ttg.dnd5.dto.base.filters.PredicateUtils;
import club.ttg.dnd5.dto.base.filters.QueryFilter;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.PathBuilder;
import lombok.experimental.UtilityClass;
import org.springframework.util.StringUtils;

import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class ClassPredicateBuilder
{
    private static final QCharacterClass Q = QCharacterClass.characterClass;

    public BooleanBuilder build(final ClassQueryRequest request)
    {
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(Q.isHiddenEntity.isFalse());
        builder.and(PredicateUtils.buildTextSearch(
                request.getSearch(),
                Q.name,
                Q.english,
                Q.alternative
        ));

        // Подклассы в общий список не идут: их отдаёт своя ручка, а раздел «Классы»
        // показывает только классы. Поиск по названию — исключение: игрок ищет
        // «Мистического рыцаря», не помня, что это подкласс воина.
        //
        // Условие ЗДЕСЬ, а не отбором над готовой страницей: отбор после пагинации
        // выбрасывает записи из уже нарезанной страницы, и запрос первых тридцати
        // возвращал четыре класса из семнадцати — остальные двадцать шесть мест
        // занимали подклассы.
        if (!StringUtils.hasText(request.getSearch()))
        {
            builder.and(Q.parentUrl.isNull());
        }

        PathBuilder<Object> characterClass = new PathBuilder<>(Object.class, Q.getMetadata());

        applyEnumOrdinalFilter(
                builder,
                request.getHitDice(),
                characterClass.getNumber("hit_dice", Integer.class),
                Dice.class
        );

        PredicateUtils.applySourcesFilter(builder, request.getSource(), "characterClass", "source");
        PredicateUtils.applyStringFilter(builder, request.getSrdVersion(), Q.srdVersion);

        return builder;
    }

    public void applyEnumOrdinalFilter(
            final BooleanBuilder builder,
            final QueryFilter<?> filter,
            final NumberPath<Integer> path,
            final Class<? extends Enum<?>> enumClass)
    {
        if (filter == null || !filter.isActive())
        {
            return;
        }

        Set<Integer> ordinals = filter.getValues().stream()
                .map(v -> {
                    if (v instanceof String str)
                    {
                        return Enum.valueOf((Class) enumClass, str).ordinal();
                    }
                    return ((Enum<?>) v).ordinal();
                })
                .collect(Collectors.toSet());

        if (filter.isExclude())
        {
            builder.and(path.notIn(ordinals));
        }
        else if (filter.isUnion())
        {
            BooleanBuilder orBuilder = new BooleanBuilder();
            for (Integer ordinal : ordinals)
            {
                orBuilder.or(path.eq(ordinal));
            }
            builder.and(orBuilder);
        }
        else
        {
            builder.and(path.in(ordinals));
        }
    }
}