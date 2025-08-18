package club.ttg.dnd5.domain.full_text_search.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.NameResponse;
import club.ttg.dnd5.domain.common.model.SectionType;
import club.ttg.dnd5.dto.base.SourceResponse;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class FullTextSearchViewDto {

    private String url;
    private NameResponse name = new NameResponse();
    private SectionType type;
    private SourceResponse source;
}
