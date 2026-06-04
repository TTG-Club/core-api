package club.ttg.dnd5.domain.charlist.rest.dto;

import club.ttg.dnd5.domain.charlist.model.CharlistVisibility;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CharlistRequest {
    @NotBlank(message = "Имя персонажа обязательно")
    private String characterName;

    private Integer characterLevel;

    private String characterClass;

    /** JSON-данные чарлиста */
    private String data;

    private CharlistVisibility visibility;
}
