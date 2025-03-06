package club.ttg.dnd5.domain.common.rest.dto.engine;

import lombok.Getter;

import java.util.Collection;

@Getter
public class FilterDto {
    private String type;
    private Collection<String> values;
}
