package club.ttg.dnd5.dto.character;

import club.ttg.dnd5.dto.ChangedDto;
import club.ttg.dnd5.dto.NameDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@JsonInclude(JsonInclude.Include.NON_NULL)

@Getter
@Builder
public class ClassResponse {
    private String url;
    private NameDto name;
    private ChangedDto changed;
}
