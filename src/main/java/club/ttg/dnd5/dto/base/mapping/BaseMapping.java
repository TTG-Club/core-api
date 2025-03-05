package club.ttg.dnd5.dto.base.mapping;

import org.mapstruct.Mapping;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface BaseMapping {

    @Retention(RetentionPolicy.SOURCE)
    @Mapping(source = "source.type.group", target = "source.group.name")
    @Mapping(source = "source.type.label", target = "source.group.label")
    @Mapping(source = "source.name", target = "source.name.name")
    @Mapping(source = "source.englishName", target = "source.name.english")
    @Mapping(source = "source.sourceAcronym", target = "source.name.label")
    @interface BaseSourceMapping {}

    @Retention(RetentionPolicy.SOURCE)
    @Mapping(source = "name", target = "name.name")
    @Mapping(source = "english", target = "name.english")
    @interface BaseShortResponseNameMapping {}

}
