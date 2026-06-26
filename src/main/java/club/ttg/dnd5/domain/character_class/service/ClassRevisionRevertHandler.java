package club.ttg.dnd5.domain.character_class.service;

import club.ttg.dnd5.domain.character_class.rest.dto.ClassRequest;
import club.ttg.dnd5.domain.revision.service.RevisionRevertHandler;
import club.ttg.dnd5.exception.ApiException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClassRevisionRevertHandler implements RevisionRevertHandler {

    private final ClassService classService;
    private final ObjectMapper objectMapper;

    @Override
    public String entityType() {
        return ClassService.REVISION_ENTITY_TYPE;
    }

    @Override
    public void revert(String entityId, String snapshotJson) {
        ClassRequest request;
        try {
            request = objectMapper.readValue(snapshotJson, ClassRequest.class);
        } catch (JsonProcessingException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Не удалось прочитать снимок класса для отката");
        }
        classService.update(entityId, request);
    }
}
