package club.ttg.dnd5.domain.species.service;

import club.ttg.dnd5.domain.common.dictionary.CreatureType;
import club.ttg.dnd5.domain.species.model.QSpecies;
import club.ttg.dnd5.domain.species.rest.dto.SpeciesQueryRequest;
import club.ttg.dnd5.dto.base.filters.PredicateUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.StringPath;
import lombok.experimental.UtilityClass;
import org.springframework.util.StringUtils;

@UtilityClass
public class SpeciesPredicateBuilder
{
    private static final QSpecies Q = QSpecies.species;
    private static final StringPath TYPE_PATH = Expressions.stringPath("type");

    // Родительский вид — по КОЛОНКЕ, а не по связи Q.parent. Поиск идёт нативным
    // запросом (JPASQLQuery), и имя пути там становится именем колонки как есть:
    // связь parent дала бы «species.parent», а колонка называется parent_url.
    private static final StringPath PARENT_URL_PATH =
            new PathBuilder<>(Object.class, Q.getMetadata()).getString("parent_url");

    public BooleanBuilder build(final SpeciesQueryRequest request)
    {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(Q.isHiddenEntity.isFalse());
        builder.and(PredicateUtils.buildTextSearch(request.getSearch(), Q.name, Q.english, Q.alternative));

        // Подвиды в общий список не идут: их показывает карточка самого вида. Поиск по
        // названию — исключение: игрок ищет «Дроу», не помня, что это подвид эльфа.
        //
        // Условие ЗДЕСЬ, а не отбором над готовой страницей: отбор после пагинации
        // выбрасывает записи из уже нарезанной страницы, и запрос первых тридцати
        // возвращал бы горстку видов — остальные места занимали бы подвиды.
        if (!StringUtils.hasText(request.getSearch()))
        {
            builder.and(PARENT_URL_PATH.isNull());
        }
        PredicateUtils.applyFilterEnum(builder, request.getCreatureType(), TYPE_PATH, CreatureType.class);
        PredicateUtils.applySourcesFilter(builder, request.getSource(), "species", "source");
        PredicateUtils.applyStringFilter(builder, request.getSrdVersion(), Q.srdVersion);
        return builder;
    }
}
