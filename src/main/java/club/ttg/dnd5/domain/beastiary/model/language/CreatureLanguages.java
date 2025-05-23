package club.ttg.dnd5.domain.beastiary.model.language;

import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
public class CreatureLanguages {
    private Collection<CreatureLanguage> languages;
    private String text;
    private String telepathy;
}
