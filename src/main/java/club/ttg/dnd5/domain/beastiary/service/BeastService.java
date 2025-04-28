package club.ttg.dnd5.domain.beastiary.service;

import club.ttg.dnd5.domain.beastiary.rest.dto.BeastDetailResponse;
import club.ttg.dnd5.domain.beastiary.rest.dto.BeastRequest;
import club.ttg.dnd5.domain.beastiary.rest.dto.BeastShortResponse;

import java.util.List;

public interface BeastService {
    Boolean existOrThrow(String url);

    List<BeastShortResponse> search(String searchLine);

    BeastDetailResponse findDetailedByUrl(String url);

    BeastRequest findFormByUrl(String url);

    String save(BeastRequest request);

    String update(String url, BeastRequest request);

    String delete(String url);
}
