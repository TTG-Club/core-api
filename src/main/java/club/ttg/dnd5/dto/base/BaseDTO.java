package club.ttg.dnd5.dto.base;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

@Getter
@Setter
public abstract class BaseDTO extends BaseUrl {
    @JsonProperty(value = "name")
    private NameBasedDTO nameBasedDTO = new NameBasedDTO();
    @Column(columnDefinition = "TEXT")
    private String description;
    @JsonProperty(value = "source")
    private SourceResponse sourceDTO = new SourceResponse();
    @LastModifiedDate
    private Instant updatedAt;
    private String userId;
}
