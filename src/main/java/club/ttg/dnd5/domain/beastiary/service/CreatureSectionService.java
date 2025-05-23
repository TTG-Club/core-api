package club.ttg.dnd5.domain.beastiary.service;

import club.ttg.dnd5.domain.beastiary.rest.dto.section.CreatureSectionDetailResponse;
import club.ttg.dnd5.domain.beastiary.rest.dto.section.CreatureSectionShortResponse;
import club.ttg.dnd5.domain.beastiary.rest.dto.section.CretureSectionRequest;

import java.util.List;

public interface CreatureSectionService {
    Boolean existOrThrow(String url);

    List<CreatureSectionShortResponse> search(String searchLine);

    CreatureSectionDetailResponse findDetailedByUrl(String url);

    CretureSectionRequest findFormByUrl(String url);

    String save(CretureSectionRequest request);

    String update(String url, CretureSectionRequest request);

    String delete(String url);
}
