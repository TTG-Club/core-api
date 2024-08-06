package club.ttg.dnd5.dictionary.beastiary;

import lombok.Getter;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public enum Condition {
    BLINDED(8, "ослепление", "слепота"),
    CHARMED(11, "очарование", "очарован"),
    DYING(-1,"смерть"),
    EVASIONS(-2, "уклонение"),
    DEAFENED(5, "глухота", "оглохший"),
    EXHAUSTION(10, "истощение", "истощенный"),
    FRIGHTENED(2, "испуг", "страх", "испуган"),
    GRAPPLED(15, "захват", "схваченный"),
    INCAPACITATED(4, "недееспособность"),
    INVISIBLE(3, "невидимый"),
    PARALYZED(13, "паралич", "парализован"),
    PETRIFIED(6, "окаменение", "окаменен"),
    POISONED(9, "отравление", "отравлен"),
    PRONE(14, "сбивание с ног", "Сбитый с ног / Лежащий ничком"),
    RESTRAINED(7, "опутанность"),
    STUNNED(12, "ошеломление"),
    UNCONSCIOUS(1, "бессознательность");

    private final String cyrillicName;
    private final Set<String> names;
    private final Integer id;

    Condition(int id, String ...  names){
        this.id = id;
        cyrillicName = names[0];
        this.names = Arrays.stream(names).collect(Collectors.toSet());
    }

    public static Set<Condition> getImmunity() {
        return EnumSet.of(
                BLINDED,
                CHARMED,
                DEAFENED,
                EXHAUSTION,
                FRIGHTENED,
                GRAPPLED,
                PARALYZED,
                PETRIFIED,
                POISONED,
                PRONE,
                RESTRAINED,
                STUNNED,
                UNCONSCIOUS);
    }

    public static Condition parse(String stateString) {
        return Arrays.stream(values())
                .filter(s -> s.getNames().stream().anyMatch(stateString::equalsIgnoreCase))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(stateString));
    }
}
