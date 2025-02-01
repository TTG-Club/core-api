package club.ttg.dnd5.dto.base.create;

import club.ttg.dnd5.dto.base.BaseUrl;
import club.ttg.dnd5.dto.base.NameBasedDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class CreateBaseDTO extends BaseUrl {
    @JsonProperty(value = "name")
    private NameBasedDTO nameBasedDTO = new NameBasedDTO();
    private String description;
    @JsonProperty(value = "source")
    private SourceReference sourceDTO = new SourceReference();
    private String userId;
}
