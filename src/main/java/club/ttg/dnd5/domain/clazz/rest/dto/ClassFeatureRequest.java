package club.ttg.dnd5.domain.clazz.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.BaseRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClassFeatureRequest extends BaseRequest {
    private short level;

    private String quote;
}
