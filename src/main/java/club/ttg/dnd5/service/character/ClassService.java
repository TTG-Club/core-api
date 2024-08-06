package club.ttg.dnd5.service.character;

import club.ttg.dnd5.dto.character.ClassRequest;
import club.ttg.dnd5.dto.character.ClassResponse;

public interface ClassService {
    ClassResponse getClass(String url);

    ClassResponse addClass(ClassRequest request);

    ClassResponse updateClass(String url, ClassRequest request);
}
