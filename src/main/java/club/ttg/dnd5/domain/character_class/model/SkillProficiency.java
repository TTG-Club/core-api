package club.ttg.dnd5.domain.character_class.model;

import club.ttg.dnd5.domain.common.dictionary.WeaponCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SkillProficiency {
    private int cnt;
    private String skills;

    @Override
    public String toString() {
        return String.format("Выберите %s навыка из следующих %s", cnt, skills);
    }
}
