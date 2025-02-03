package club.ttg.dnd5.dto.base;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public abstract class BaseDTO extends BaseUrl {
    @JsonProperty(value = "name")
    private NameBasedDTO nameBasedDTO = new NameBasedDTO();
    private String description;
    @JsonProperty(value = "source")
    private SourceResponse sourceDTO = new SourceResponse();
    private Instant updatedAt;
    private String userId;
}
