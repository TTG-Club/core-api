package club.ttg.dnd5.domain.feat.rest.dto;

import club.ttg.dnd5.domain.common.dto.BaseDto;
import club.ttg.dnd5.domain.common.dto.NameDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FeatDetailResponse extends BaseDto {
    private NameDto category;
    private String prerequisite;
}
