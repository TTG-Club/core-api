package club.ttg.dnd5.domain.beastiary.service;

import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureDetailResponse;
import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureRequest;
import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureQueryRequest;
import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureShortResponse;
import club.ttg.dnd5.dto.base.PageResponse;

public interface CreatureService {
    Boolean existOrThrow(String url);

    PageResponse<CreatureShortResponse> search(CreatureQueryRequest request);

    CreatureDetailResponse findDetailedByUrl(String url);

    CreatureRequest findFormByUrl(String url);

    String save(CreatureRequest request);

    String update(String url, CreatureRequest request);

    String delete(String url);

    CreatureDetailResponse preview(CreatureRequest request);
}

