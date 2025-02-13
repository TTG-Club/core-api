package club.ttg.dnd5.dto.page;

import club.ttg.dnd5.dto.base.BaseUrl;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.Collection;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PageResponse {
    private Collection<BaseUrl> content;
    private Long skip;
    private Long take;
    private Long totals;
}
