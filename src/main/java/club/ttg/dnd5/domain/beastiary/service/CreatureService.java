package club.ttg.dnd5.domain.beastiary.service;

import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureDetailResponse;
import club.ttg.dnd5.domain.beastiary.rest.dto.BeastRequest;
import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureShortResponse;

import java.util.List;

public interface CreatureService {
    Boolean existOrThrow(String url);

    List<CreatureShortResponse> search(String searchLine);

    CreatureDetailResponse findDetailedByUrl(String url);

    BeastRequest findFormByUrl(String url);

    String save(BeastRequest request);

    String update(String url, BeastRequest request);

    String delete(String url);
}
