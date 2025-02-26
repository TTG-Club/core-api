package club.ttg.dnd5.domain.clazz.service;

import club.ttg.dnd5.domain.clazz.rest.dto.ClassDetailResponse;
import club.ttg.dnd5.domain.clazz.rest.dto.ClassFeatureDto;
import club.ttg.dnd5.dto.engine.SearchRequest;

import java.util.Collection;

public interface ClassService {
    ClassDetailResponse getClass(String url);

    ClassDetailResponse addClass(ClassDetailResponse request);

    ClassDetailResponse updateClass(String url, ClassDetailResponse request);

    Collection<ClassDetailResponse> getClasses(SearchRequest request);

    Collection<ClassDetailResponse> getSubClasses(String url);

    ClassDetailResponse addParent(String classUrl, String classParentUrl);

    ClassDetailResponse addFeature(String classUrl, ClassFeatureDto featureDto);

    boolean exist(String url);
}
