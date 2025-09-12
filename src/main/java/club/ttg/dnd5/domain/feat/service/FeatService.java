package club.ttg.dnd5.domain.feat.service;

import club.ttg.dnd5.domain.common.rest.dto.PageResponse;
import club.ttg.dnd5.domain.feat.rest.dto.FeatDetailResponse;
import club.ttg.dnd5.domain.feat.rest.dto.FeatRequest;
import club.ttg.dnd5.domain.feat.rest.dto.FeatShortResponse;
import club.ttg.dnd5.domain.filter.model.SearchBody;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

public interface FeatService {
    FeatDetailResponse getFeat(String featUrl);

    PageResponse<FeatShortResponse> getFeats(final @Valid @Size String searchLine, final int page, final int limit, final String[] sort, final SearchBody searchBody);

    String addFeat(FeatRequest featDto);

    String updateFeat(final String featUrl, FeatRequest featDto);

    String delete(String featUrl);

    boolean existOrThrow(String featUrl);

    FeatRequest findFormByUrl(String url);

    FeatDetailResponse preview(FeatRequest request);
}
