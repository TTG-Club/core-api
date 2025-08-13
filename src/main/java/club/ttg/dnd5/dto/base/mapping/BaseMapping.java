package club.ttg.dnd5.dto.base.mapping;

import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

@Mapper(componentModel = "spring")
public interface BaseMapping {

    @Retention(RetentionPolicy.SOURCE)
    @Mapping(source = "source.type.group", target = "source.group.name")
    @Mapping(source = "source.type.label", target = "source.group.label")
    @Mapping(source = "source.name", target = "source.name.name")
    @Mapping(source = "source.englishName", target = "source.name.english")
    @Mapping(source = "source.sourceAcronym", target = "source.name.label")
    @Mapping(source = "sourcePage", target = "source.page")
    @interface BaseSourceMapping {}

    @Retention(RetentionPolicy.SOURCE)
    @Mapping(source = "name", target = "name.name")
    @Mapping(source = "english", target = "name.english")
    @interface BaseShortResponseNameMapping {}


    @Mapping(source = "request.name.name", target = "name")
    @Mapping(source = "request.name.english", target = "english")
    @Mapping(source = "request.name.alternative", target = "alternative", qualifiedByName = "collectToString")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "source.createdAt", ignore = true)
    @Mapping(target = "source.updatedAt", ignore = true)
    @interface BaseEntityNameMapping {}

    @Mapping(source = "name", target = "name.name")
    @Mapping(source = "english", target = "name.english")
    @Mapping(source = "alternative", target = "name.alternative", qualifiedByName = "altToCollection")
    @interface BaseRequestNameMapping {}

    @Mapping(source = "sourcePage", target = "source.page")
    @Mapping(source = "source.url", target = "source.url")
    @interface BaseSourceRequestMapping{}

    @Named("collectToString")
    default String collectToString(Collection<String> names) {
        return Optional.ofNullable(names).map(name -> String.join(";", name)).orElse("");
    }

    @Named("altToCollection")
    default Collection<String> altToCollection(String string) {
        if(StringUtils.isEmpty(string)) {
            return Collections.emptyList();
        }
        return Arrays.asList(string.split(";"));
    }
}
