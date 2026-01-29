package club.ttg.dnd5.domain.beastiary.service;

import club.ttg.dnd5.domain.beastiary.CreatureGroupType;
import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureDetailResponse;
import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureRequest;
import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureShortResponse;
import club.ttg.dnd5.domain.common.rest.dto.container.ContainerResponse;
import club.ttg.dnd5.domain.filter.model.SearchBody;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.util.List;

public interface CreatureService {
    Boolean existOrThrow(String url);

    List<CreatureShortResponse> search(String searchLine, final SearchBody searchBody);

    CreatureDetailResponse findDetailedByUrl(String url);

    CreatureRequest findFormByUrl(String url);

    String save(CreatureRequest request);

    String update(String url, CreatureRequest request);

    String delete(String url);

    CreatureDetailResponse preview(CreatureRequest request);

    List<CreatureShortResponse> search(@Valid @Size(min = 2) String searchLine, String searchBody);

    ContainerResponse<CreatureShortResponse> search(@Valid @Size(min = 2)
                                                    String searchLine,
                                                    String filter,
                                                    CreatureGroupType group,
                                                    String sort,
                                                    long limit,
                                                    long skip);
}
