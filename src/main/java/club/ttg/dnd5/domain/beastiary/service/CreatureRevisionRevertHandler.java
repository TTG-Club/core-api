package club.ttg.dnd5.domain.beastiary.service;

import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureRequest;
import club.ttg.dnd5.domain.revision.service.RevisionRevertHandler;
import club.ttg.dnd5.exception.ApiException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreatureRevisionRevertHandler implements RevisionRevertHandler {

    private final CreatureService creatureService;
    private final ObjectMapper objectMapper;

    @Override
    public String entityType() {
        return CreatureServiceImpl.REVISION_ENTITY_TYPE;
    }

    @Override
    public void revert(String entityId, String snapshotJson) {
        CreatureRequest request;
        try {
            request = objectMapper.readValue(snapshotJson, CreatureRequest.class);
        } catch (JsonProcessingException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Не удалось прочитать снимок существа для отката");
        }
        creatureService.update(entityId, request);
    }
}
