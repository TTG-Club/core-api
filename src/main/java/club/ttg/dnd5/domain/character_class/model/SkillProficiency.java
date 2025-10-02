package club.ttg.dnd5.domain.character_class.model;

import club.ttg.dnd5.domain.common.dictionary.Skill;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collection;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SkillProficiency {
    private int cnt;
    private Collection<Skill> skills;
    private String text;

    @Override
    public String toString() {
        return String.format("Выберите %s навыка из следующих %s", cnt, skills);
    }
}
