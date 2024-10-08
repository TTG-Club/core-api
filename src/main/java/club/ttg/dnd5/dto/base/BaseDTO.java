package club.ttg.dnd5.dto.base;

import lombok.Getter;
import lombok.Setter;

/*
 В @Override можно будет добавлять кастомную логику, если понадобится
 */
@Getter
@Setter
public abstract class BaseDTO {
    private String url;
    private String imageUrl;
    private NameBasedDTO nameBasedDTO;
    private SourceResponse source;
}
