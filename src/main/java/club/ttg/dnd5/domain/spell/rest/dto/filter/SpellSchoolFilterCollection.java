package club.ttg.dnd5.domain.spell.rest.dto.filter;

import club.ttg.dnd5.domain.spell.model.enums.MagicSchool;
import club.ttg.dnd5.dto.base.filters.AbstractFilteringCollection;
import club.ttg.dnd5.dto.base.filters.AbstractFilteringField;
import club.ttg.dnd5.jooq.tables.JooqSpell;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jooq.TableField;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class SpellSchoolFilterCollection extends AbstractFilteringCollection<SpellSchoolFilterCollection.SpellSchoolFilterField> {

    private static final String LABEL = "Школа";

    public SpellSchoolFilterCollection(List<SpellSchoolFilterField> fields, String label) {
        super(fields, label);
    }

    public static SpellSchoolFilterCollection getDefault() {
        List<SpellSchoolFilterField> fields = MagicSchool.getVALUES().stream()
                .map(s -> new SpellSchoolFilterField(s, s.getName(), true))
                .collect(Collectors.toList());
        return new SpellSchoolFilterCollection(fields, LABEL);
    }

    @Override
    protected TableField<?, ?> getPath() {
        return JooqSpell.SPELL.SCHOOL;
    }

    @Getter
    @Setter
    public static class SpellSchoolFilterField extends AbstractFilteringField<MagicSchool> {

        public SpellSchoolFilterField(MagicSchool i, String label, boolean selected) {
            super(i, label, selected);
        }
    }

}
