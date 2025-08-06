package club.ttg.dnd5.domain.full_text_search.rest.dto;

import club.ttg.dnd5.domain.full_text_search.model.FullTextSearchViewType;
import club.ttg.dnd5.dto.base.SourceResponse;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class FullTextSearchViewDto {

    private String url;
    private FullTextSearchNameResponse name = new FullTextSearchNameResponse();
    private FullTextSearchViewType type;
    private SourceResponse source;
}
