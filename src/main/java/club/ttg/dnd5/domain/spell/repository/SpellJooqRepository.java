package club.ttg.dnd5.domain.spell.repository;

import club.ttg.dnd5.dto.base.filters.AbstractFilteringCollection;
import club.ttg.dnd5.dto.base.filters.FilterDto;
import club.ttg.dnd5.jooq.tables.JooqSpell;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SpellJooqRepository {
    private final DSLContext dslContext;
    private static final JooqSpell SPELL = JooqSpell.SPELL;

    public void searchSpells(FilterDto filterDto) {
        List<Record> authors = dslContext.select()
                .from(SPELL)
                .where(filterDto.getFilters().stream().map(AbstractFilteringCollection::getQuery).flatMap(Collection::stream).collect(Collectors.toSet()))
                .fetch();
    }
}
