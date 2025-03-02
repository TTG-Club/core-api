package club.ttg.dnd5.domain.clazz.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.BaseResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClassFeatureRequest extends BaseResponse {
    private short level;
}
