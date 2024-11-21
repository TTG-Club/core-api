package club.ttg.dnd5.service.character;

import club.ttg.dnd5.dto.character.FeatDto;

import java.util.Collection;

public interface FeatService {
    FeatDto getFeat(String featUrl);

    Collection<FeatDto> getFeats();

    FeatDto addFeat(FeatDto featDto);

    FeatDto updateFeat(final String featUrl, FeatDto featDto);

    FeatDto delete(String featUrl);
}
