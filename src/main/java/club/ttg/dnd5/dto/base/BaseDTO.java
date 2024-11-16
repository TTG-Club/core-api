package club.ttg.dnd5.dto.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/*
 В @Override можно будет добавлять кастомную логику, если понадобится
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public abstract class BaseDTO {
    private String url;
    private String imageUrl;
    @JsonProperty("name")
    private NameBasedDTO nameBasedDTO = new NameBasedDTO();
    private String description;
    private SourceResponse sourceDTO = new SourceResponse();
}
