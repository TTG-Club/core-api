package club.ttg.dnd5.domain.character_class.rest.dto;

import club.ttg.dnd5.domain.character_class.model.CasterType;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class MulticlassInfo {
    @JsonProperty("class")
    private String name;
    private String subclass;
    private int level;
    private String hitDice;

    @Schema(description = "Тип заклинателя сегмента: свой у класса-заклинателя, иначе подкласса. "
            + "По PACT фронт считает уровень Магии договора отдельно от общего уровня заклинателя", example = "PACT")
    private CasterType casterType;
}
