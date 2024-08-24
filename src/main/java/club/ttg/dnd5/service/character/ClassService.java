package club.ttg.dnd5.service.character;

import club.ttg.dnd5.dto.character.ClassRequest;
import club.ttg.dnd5.dto.character.ClassResponse;
import club.ttg.dnd5.dto.engine.SearchRequest;

import java.util.Collection;

public interface ClassService {
    ClassResponse getClass(String url);

    ClassResponse addClass(ClassRequest request);

    ClassResponse updateClass(String url, ClassRequest request);

    Collection<ClassResponse> getClasses(SearchRequest request);

    Collection<ClassResponse> getSubClasses(String url);
}
