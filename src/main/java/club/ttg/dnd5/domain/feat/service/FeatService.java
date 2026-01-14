package club.ttg.dnd5.domain.feat.service;

import club.ttg.dnd5.domain.feat.model.FeatCategory;
import club.ttg.dnd5.domain.feat.rest.dto.FeatDetailResponse;
import club.ttg.dnd5.domain.feat.rest.dto.FeatRequest;
import club.ttg.dnd5.domain.feat.rest.dto.FeatSelectResponse;
import club.ttg.dnd5.domain.feat.rest.dto.FeatShortResponse;
import club.ttg.dnd5.domain.filter.model.SearchBody;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.util.Collection;
import java.util.Set;

public interface FeatService {
    FeatDetailResponse getFeat(String featUrl);

    Collection<FeatShortResponse> search(@Valid @Size(min = 2) String searchLine, String filter);

    Collection<FeatShortResponse> getFeats(final @Valid @Size String searchLine, final SearchBody searchBody);

    String addFeat(FeatRequest featDto);

    String updateFeat(final String featUrl, FeatRequest featDto);

    String delete(String featUrl);

    boolean existOrThrow(String featUrl);

    FeatRequest findFormByUrl(String url);

    FeatDetailResponse preview(FeatRequest request);

    Collection<FeatSelectResponse> getFeatsSelect(final @Valid @Size String searchLine, final Set<FeatCategory> categories);
}
