package club.ttg.dnd5.dto.base.create;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class SourceReference {
    @JsonProperty(namespace = "url")
    private String url;
    private int page;
}
