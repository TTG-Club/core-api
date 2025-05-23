package club.ttg.dnd5.domain.beastiary.model.language;

import club.ttg.dnd5.domain.common.dictionary.Language;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatureLanguage {
    private Language language;
    private String text;
}
