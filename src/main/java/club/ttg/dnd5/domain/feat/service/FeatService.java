package club.ttg.dnd5.domain.feat.service;

import club.ttg.dnd5.domain.feat.rest.dto.FeatDetailResponse;

import java.util.Collection;

public interface FeatService {
    FeatDetailResponse getFeat(String featUrl);

    Collection<FeatDetailResponse> getFeats();

    FeatDetailResponse addFeat(FeatDetailResponse featDto);

    FeatDetailResponse updateFeat(final String featUrl, FeatDetailResponse featDto);

    FeatDetailResponse delete(String featUrl);

    boolean exists(String featUrl);
}
