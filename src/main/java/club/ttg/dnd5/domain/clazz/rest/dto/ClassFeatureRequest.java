package club.ttg.dnd5.domain.clazz.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.BaseDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClassFeatureRequest extends BaseDto {
    private short level;
}
