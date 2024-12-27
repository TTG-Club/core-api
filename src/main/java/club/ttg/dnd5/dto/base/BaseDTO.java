package club.ttg.dnd5.dto.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public abstract class BaseDTO  {
    private String url;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty(value = "image")
    private String imageUrl;
    @JsonProperty(value = "name")
    private NameBasedDTO nameBasedDTO = new NameBasedDTO();
    private String description;
    @JsonProperty(value = "source")
    private SourceResponse sourceDTO = new SourceResponse();
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<String> gallery = new ArrayList<>();
    private Instant updatedAt;
    private String userId;
}


