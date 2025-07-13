package club.ttg.dnd5.domain.beastiary.model.enumus;

import club.ttg.dnd5.exception.ApiException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CreatureTraits {
    LEGENDARY_RESISTANCE("Легендарное сопротивление"),
    MAGIC_RESISTANCE("Сопротивление магии"),
    BLOODIED_FRENZY("Кровавое безумие"),
    EVASION("Увёртливость"),
    FESTERING_AURA("Зловонная аура"),
    INCORPOREAL_PASSAGE("Бестелесное перемещение"),
    OATHSOME_LIMBS("Мерзкие конечности"),
    REGENERATION("Регенерация"),
    AURA_OF_BRAVERY("Аура храбрости"),
    AMPHIBIOUS_("Амфибия"),
    FLYBY("Облёт"),
    SPIDER_CLIMB("Паучье лазание"),
    HEATED_BODY("Нагретое тело"),
    STONY_LETHARGY("Каменная летаргия"),
    AMORPHOUS("Аморфный"),
    DEATH_THROES("Предсмертная вспышка"),
    SWARM("Рой"),
    SHARED_RESISTANCES("Общее сопротивление"),
    INCORPOREAL_MOVEMENT("Бестелесное перемещение"),
    LIGHT_SENSITIVITY("Чувствительность к солнечному свету");

    private final String name;

    public static CreatureTraits parse(String name) {
        for (CreatureTraits traits : values()) {
            if (traits.name.equalsIgnoreCase(name)) {
                return traits;
            }
        }
        throw new ApiException(HttpStatus.NOT_FOUND, "Неправильное умение");
    }
}
