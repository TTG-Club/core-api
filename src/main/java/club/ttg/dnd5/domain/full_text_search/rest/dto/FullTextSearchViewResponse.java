package club.ttg.dnd5.domain.full_text_search.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.NameResponse;
import club.ttg.dnd5.domain.full_text_search.model.FullTextSearchViewType;
import club.ttg.dnd5.dto.base.SourceResponse;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class FullTextSearchViewResponse {

    private String url;
    private NameResponse name = new NameResponse();
    private FullTextSearchViewType type;
    private SourceResponse source;
}
