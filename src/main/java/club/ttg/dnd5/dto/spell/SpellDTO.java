package club.ttg.dnd5.dto.spell;

import club.ttg.dnd5.dto.base.BaseDTO;
import club.ttg.dnd5.dto.spell.component.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonRootName("spell")
public class SpellDTO extends BaseDTO {
    @JsonProperty("level")
    private int level; // Уровень заклинания

    @JsonProperty("school")
    private MagicSchoolDTO school; // Школа магии

    @JsonProperty("distance")
    private SpellDistanceDTO distance; // Дистанция заклинания

    @JsonProperty("duration")
    private SpellDurationDTO duration; // Длительность

    @JsonProperty("time")
    private SpellCastingTimeDTO time; // Время накладывания

    @JsonProperty("components")
    private SpellComponentsDTO components; // Компоненты заклинания

    @JsonProperty("affiliation")
    private SpellAffiliationDTO affiliation; // Привязка к классам

    @JsonProperty("tags")
    private Set<String> tags = new HashSet<>(); // Теги заклинания
}
