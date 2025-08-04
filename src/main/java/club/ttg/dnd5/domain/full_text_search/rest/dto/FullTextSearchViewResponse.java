package club.ttg.dnd5.domain.full_text_search.rest.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class FullTextSearchViewResponse {

    List<FullTextSearchViewDto> result;

    Long total;
}
