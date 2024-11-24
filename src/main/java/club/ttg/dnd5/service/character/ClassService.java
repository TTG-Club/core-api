package club.ttg.dnd5.service.character;

import club.ttg.dnd5.dto.character.ClassDto;
import club.ttg.dnd5.dto.character.ClassFeatureDto;
import club.ttg.dnd5.dto.engine.SearchRequest;

import java.util.Collection;

public interface ClassService {
    ClassDto getClass(String url);

    ClassDto addClass(ClassDto request);

    ClassDto updateClass(String url, ClassDto request);

    Collection<ClassDto> getClasses(SearchRequest request);

    Collection<ClassDto> getSubClasses(String url);

    ClassDto addParent(String classUrl, String classParentUrl);

    ClassDto addFeature(String classUrl, ClassFeatureDto featureDto);
}
