package club.ttg.dnd5.domain.common.rest.dto;

import club.ttg.dnd5.domain.common.dictionary.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SizeDto {
    private Size size;
    private String suffix;
}
