package club.ttg.dnd5.dto.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BaseDTO {
    private String url;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String imageUrl;
    @JsonProperty(value = "name")
    private NameBasedDTO nameBasedDTO = new NameBasedDTO();
    private String description;
    @JsonProperty(value = "source")
    private SourceResponse sourceDTO = new SourceResponse();
}


