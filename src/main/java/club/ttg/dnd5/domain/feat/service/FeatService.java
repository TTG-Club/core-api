package club.ttg.dnd5.domain.feat.service;

import club.ttg.dnd5.domain.common.rest.dto.ShortResponse;
import club.ttg.dnd5.domain.feat.rest.dto.FeatDetailResponse;
import club.ttg.dnd5.domain.feat.rest.dto.FeatRequest;
import club.ttg.dnd5.domain.feat.rest.dto.FeatShortResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.util.Collection;

public interface FeatService {
    FeatDetailResponse getFeat(String featUrl);

    Collection<FeatShortResponse> getFeats(final @Valid @Size String searchLine);

    FeatDetailResponse addFeat(FeatRequest featDto);

    FeatDetailResponse updateFeat(final String featUrl, FeatRequest featDto);

    ShortResponse delete(String featUrl);

    boolean exists(String featUrl);
}
