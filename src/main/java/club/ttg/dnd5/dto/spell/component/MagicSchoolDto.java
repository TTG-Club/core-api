package club.ttg.dnd5.dto.spell.component;

import club.ttg.dnd5.dto.base.NameBasedDTO;
import club.ttg.dnd5.dto.book.SourceBookDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonRootName("magic_school")
public class MagicSchoolDto {

    @JsonProperty("name")
    private NameBasedDTO name; // Название школы

    @JsonProperty("description")
    private String description; // Описание

    @JsonProperty("source")
    private SourceBookDTO source; // Источник
}
