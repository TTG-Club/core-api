package club.ttg.dnd5.domain.beastiary.service;

import club.ttg.dnd5.domain.beastiary.rest.dto.BeastDetailResponse;
import club.ttg.dnd5.domain.beastiary.rest.dto.BeastRequest;
import club.ttg.dnd5.domain.beastiary.rest.dto.BeastShortResponse;
import club.ttg.dnd5.domain.spell.rest.dto.SpellDetailedResponse;
import club.ttg.dnd5.domain.spell.rest.dto.SpellShortResponse;
import club.ttg.dnd5.domain.spell.rest.dto.create.SpellRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

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
