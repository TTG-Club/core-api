package club.ttg.dnd5.domain.clazz.service;

import club.ttg.dnd5.domain.clazz.rest.dto.*;
import club.ttg.dnd5.domain.common.rest.dto.engine.SearchRequest;

import java.util.Collection;

public interface ClassService {
    ClassDetailResponse getClass(String url);

    ClassDetailResponse addClass(ClassRequest request);

    ClassDetailResponse updateClass(String url, ClassRequest request);

    Collection<ClassShortResponse> getClasses(SearchRequest request);

    Collection<ClassShortResponse> getSubClasses(String url);

    ClassDetailResponse addParent(String classUrl, String classParentUrl);

    ClassDetailResponse addFeature(String classUrl, ClassFeatureRequest featureDto);

    boolean exist(String url);
}
