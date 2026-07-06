package club.ttg.dnd5.domain.roadmap.service;

import club.ttg.dnd5.domain.revision.service.RevisionRevertHandler;
import club.ttg.dnd5.domain.roadmap.rest.dto.RoadmapRequest;
import club.ttg.dnd5.exception.ApiException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoadmapRevisionRevertHandler implements RevisionRevertHandler {

    private final RoadmapService roadmapService;
    private final ObjectMapper objectMapper;

    @Override
    public String entityType() {
        return RoadmapService.REVISION_ENTITY_TYPE;
    }

    @Override
    public void revert(String entityId, String snapshotJson) {
        RoadmapRequest request;
        try {
            request = objectMapper.readValue(snapshotJson, RoadmapRequest.class);
        } catch (JsonProcessingException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Не удалось прочитать снимок дорожной карты для отката");
        }
        roadmapService.update(entityId, request);
    }
}
