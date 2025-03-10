package club.ttg.dnd5.domain.feat.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.BaseResponse;
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
public class FeatDetailResponse extends BaseResponse {
    private String category;
    private String prerequisite;
}
