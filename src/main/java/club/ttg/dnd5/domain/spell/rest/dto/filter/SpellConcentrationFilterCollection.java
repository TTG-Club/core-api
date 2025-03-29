package club.ttg.dnd5.domain.spell.rest.dto.filter;

import club.ttg.dnd5.dto.base.filters.AbstractFilteringCollection;
import club.ttg.dnd5.dto.base.filters.AbstractFilteringField;
import club.ttg.dnd5.jooq.tables.JooqSpell;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jooq.Condition;
import org.jooq.TableField;
import org.jooq.impl.DSL;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class SpellConcentrationFilterCollection extends AbstractFilteringCollection<SpellConcentrationFilterCollection.SpellConcentrationFilterField> {

    private static final String LABEL = "Уровень";

    public SpellConcentrationFilterCollection(List<SpellConcentrationFilterField> fields, String label) {
        super(fields, label);
    }

    public static SpellConcentrationFilterCollection getDefault() {
        List<SpellConcentrationFilterField> fields = List.of(new SpellConcentrationFilterField(true, "требуется", null),
                new SpellConcentrationFilterField(false, "требуется", null));
        return new SpellConcentrationFilterCollection(fields, LABEL);
    }

    @Override
    public Condition getPositiveQuery() {
        return DSL.condition("exists (select 1 from jsonb_array_elements(duration) as elem where (elem->>'concentration') in ({0}))", getPositiveFields().stream().map(Objects ::toString)
                .collect(Collectors.joining(",")));
    }
    @Override
    public Condition getNegativeQuery() {
        return DSL.condition("exists (select 1 from jsonb_array_elements(duration) as elem where (elem->>'concentration') not in ({0}))", getPositiveFields().stream().map(Objects ::toString)
                .collect(Collectors.joining(",")));
    }

    @Override
    protected TableField<?, ?> getPath() {
        return JooqSpell.SPELL.DURATION;
    }




    @Getter
    @Setter
    public static class SpellConcentrationFilterField extends AbstractFilteringField<Boolean> {

        public SpellConcentrationFilterField(Boolean i, String label, Boolean selected) {
            super(i, label, selected);
        }
    }

}
