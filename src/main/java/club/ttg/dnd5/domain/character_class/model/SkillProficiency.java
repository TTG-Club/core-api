package club.ttg.dnd5.domain.character_class.model;

import club.ttg.dnd5.domain.common.dictionary.Skill;
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
            return "Выберите любые %d".formatted(count);
        }
        return "Выберите %d навыка из следующих %s".formatted(
                count,
                skills.stream()
                        .map(Skill::getName)
                        .collect(Collectors.joining(", ")));
    }
}
