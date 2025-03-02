package club.ttg.dnd5.dto.base.create;

import club.ttg.dnd5.domain.common.model.TagType;
import club.ttg.dnd5.domain.common.rest.dto.NameResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class CreateBaseDTO extends TagType.BaseUrl {
    @JsonProperty(value = "name")
    private NameResponse nameBasedDTO = new NameResponse();
    private String description;
    @JsonProperty(value = "source")
    private SourceReference sourceDTO = new SourceReference();
    private String userId;
}
