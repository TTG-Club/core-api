package club.ttg.dnd5.domain.common.dictionary;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum Condition {
    BLINDED("ослеплённый"),
    POISONED("отравленный"),
    INCAPACITATED("недееспособный"),
    DEAFENED("оглохший"),
    RESTRAINED( "опутанный"),
    PARALYZED("парализованный"),
    FRIGHTENED("испуганный"),
    UNCONSCIOUS( "бессознательный"),
    GRAPPLED("схваченный"),
    CHARMED("очарованный"),
    PRONE("лежащий ничком"),
    INVISIBLE("невидимый"),
    EXHAUSTION("истощённый"),
    STUNNED("ошеломлённый"),
    PETRIFIED("окаменевший");

    private final String name;

}
