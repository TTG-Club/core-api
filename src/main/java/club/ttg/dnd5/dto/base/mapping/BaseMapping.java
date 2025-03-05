package club.ttg.dnd5.dto.base.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collection;

@Mapper(componentModel = "spring")
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


    @Mapping(source = "request.name.name", target = "name")
    @Mapping(source = "request.name.english", target = "english")
    @Mapping(source = "request.name.alternative", target = "alternative", qualifiedByName = "collectToString")
    @interface BaseEntityNameMapping {}

    @Named("collectToString")
    default String collectToString(Collection<String> names) {
        return String.join(" ", names);
    }
}
