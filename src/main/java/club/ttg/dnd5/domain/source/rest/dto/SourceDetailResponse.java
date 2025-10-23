package club.ttg.dnd5.domain.source.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.BaseResponse;
import club.ttg.dnd5.dto.base.TranslationDto;
import club.ttg.dnd5.dto.base.serializer.MarkupDescriptionSerializer;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SourceDetailResponse extends BaseResponse {
    private String published;
    private String type;
    private String image;
    @JsonSerialize(using = MarkupDescriptionSerializer.class)
    private String authors;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private TranslationDto translation;
    private Set<String> tags = new HashSet<>();
}
