package club.ttg.dnd5.domain.character_class.model;

import club.ttg.dnd5.domain.common.dictionary.Skill;
import club.ttg.dnd5.util.PluralUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collection;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SkillProficiency {
    private int count;
    private Collection<Skill> skills;

    @Override
    public String toString() {
        if (skills.size() == Skill.values().length) {
            return "Выберите любые %d навыка".formatted(count);
        }
        return "Выберите %d %s из следующих: %s".formatted(
                count,
                PluralUtil.getPlural(count, new String[]{"навык", "навыка", "навыков"}),
                skills.stream()
                        .map(Skill::getName)
                        .collect(Collectors.joining(", ")));
    }
}
