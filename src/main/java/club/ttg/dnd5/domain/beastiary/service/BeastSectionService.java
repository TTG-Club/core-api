package club.ttg.dnd5.domain.beastiary.service;

import club.ttg.dnd5.domain.beastiary.rest.dto.BeastDetailResponse;
import club.ttg.dnd5.domain.beastiary.rest.dto.BeastRequest;
import club.ttg.dnd5.domain.beastiary.rest.dto.BeastShortResponse;
import club.ttg.dnd5.domain.beastiary.rest.dto.section.BeastSectionDetailResponse;
import club.ttg.dnd5.domain.beastiary.rest.dto.section.BeastSectionRequest;
import club.ttg.dnd5.domain.beastiary.rest.dto.section.BeastSectionShortResponse;

import java.util.List;

public interface BeastSectionService {
    Boolean existOrThrow(String url);

    List<BeastSectionShortResponse> search(String searchLine);

    BeastSectionDetailResponse findDetailedByUrl(String url);

    BeastSectionRequest findFormByUrl(String url);

    String save(BeastSectionRequest request);

    String update(String url, BeastSectionRequest request);

    String delete(String url);
}
