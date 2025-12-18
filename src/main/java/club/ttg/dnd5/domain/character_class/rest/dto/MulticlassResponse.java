package club.ttg.dnd5.domain.character_class.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MulticlassResponse extends ClassDetailedResponse {
    private int characterLevel;
    private int spellcastingLevel;
    private List<MulticlassInfo> multiclass;
}
