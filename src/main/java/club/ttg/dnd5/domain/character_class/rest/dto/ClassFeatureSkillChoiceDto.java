package club.ttg.dnd5.domain.character_class.rest.dto;

import club.ttg.dnd5.domain.character_class.model.SkillProficiency;
import club.ttg.dnd5.domain.common.dictionary.Skill;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Optional;

/**
 * Выбор владения навыками, который даёт само умение класса или подкласса.
 * Навыки отдаются русскими названиями: потребитель (лист персонажа) сопоставляет
 * их с навыками листа по названию, как и прозу владений класса.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClassFeatureSkillChoiceDto {

    @Schema(description = "Сколько навыков нужно выбрать", example = "1")
    private int count;

    @Schema(description = "Навыки на выбор", example = "[\"Акробатика\", \"Запугивание\"]")
    private List<String> skills;

    public ClassFeatureSkillChoiceDto(SkillProficiency skillChoice) {
        this.count = skillChoice.getCount();
        this.skills = Optional.ofNullable(skillChoice.getSkills())
                .orElse(List.of())
                .stream()
                .map(Skill::getName)
                .toList();
    }
}
