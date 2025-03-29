package club.ttg.dnd5.domain.spell.rest.dto.filter;

import club.ttg.dnd5.dto.base.filters.AbstractFilteringCollection;
import club.ttg.dnd5.dto.base.filters.AbstractFilteringField;
import club.ttg.dnd5.jooq.tables.JooqSpell;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jooq.TableField;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Getter
@Setter
@NoArgsConstructor
public class SpellLevelFilterCollection extends AbstractFilteringCollection<SpellLevelFilterCollection.SpellLevelFilterField> {

    private static final String LABEL = "Уровень";

    public SpellLevelFilterCollection(List<SpellLevelFilterField> fields, String label) {
        super(fields, label);
    }

    public static SpellLevelFilterCollection getDefault() {
        List<SpellLevelFilterField> fields = LongStream.range(0, 9).boxed()
                .map(i ->
                        new SpellLevelFilterField(i, i.equals(0L) ? "заговор" : i.toString(), true))
                .collect(Collectors.toList());
        return new SpellLevelFilterCollection(fields, LABEL);
    }

    @Override
    protected TableField<?, ?> getPath() {
        return JooqSpell.SPELL.LEVEL;
    }


    @Getter
    @Setter
    public static class SpellLevelFilterField extends AbstractFilteringField<Long> {

        public SpellLevelFilterField(Long i, String label, boolean selected) {
            super(i, label, selected);
        }
    }

}
