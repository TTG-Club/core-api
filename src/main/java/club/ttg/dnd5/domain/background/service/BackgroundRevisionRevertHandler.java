package club.ttg.dnd5.domain.background.service;

import club.ttg.dnd5.domain.background.rest.dto.BackgroundRequest;
import club.ttg.dnd5.domain.revision.service.RevisionRevertHandler;
import club.ttg.dnd5.exception.ApiException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BackgroundRevisionRevertHandler implements RevisionRevertHandler {

    private final BackgroundService backgroundService;
    private final ObjectMapper objectMapper;

    @Override
    public String entityType() {
        return BackgroundServiceImpl.REVISION_ENTITY_TYPE;
    }

    @Override
    public void revert(String entityId, String snapshotJson) {
        BackgroundRequest request;
        try {
            request = objectMapper.readValue(snapshotJson, BackgroundRequest.class);
        } catch (JsonProcessingException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Не удалось прочитать снимок предыстории для отката");
        }
        backgroundService.updateBackgrounds(entityId, request);
    }
}
