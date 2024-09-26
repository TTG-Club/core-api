package club.ttg.dnd5.dto.species;

import club.ttg.dnd5.dictionary.Size;
import club.ttg.dnd5.dictionary.beastiary.CreatureType;
import club.ttg.dnd5.dto.base.SourceResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreaturePropertiesDTO {
    private Size size;
    private CreatureType type;
    private int speed;
    private int fly;
    private int climb;
    private int swim;
    private int darkVision;
    private SourceResponse sourceResponse;
}