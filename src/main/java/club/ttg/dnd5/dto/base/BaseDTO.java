package club.ttg.dnd5.dto.base;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

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
    //подумать как сохранять в бдшке, тэги
    //Сделать поиск по тегу
    private Map<String, String> tags;
}
