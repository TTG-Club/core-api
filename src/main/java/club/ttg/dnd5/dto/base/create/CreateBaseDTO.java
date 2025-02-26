package club.ttg.dnd5.dto.base.create;

import club.ttg.dnd5.domain.common.dto.BaseUrl;
import club.ttg.dnd5.domain.common.dto.NameDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class CreateBaseDTO extends BaseUrl {
    @JsonProperty(value = "name")
    private NameDto nameBasedDTO = new NameDto();
    private String description;
    @JsonProperty(value = "source")
    private SourceReference sourceDTO = new SourceReference();
    private String userId;
}
