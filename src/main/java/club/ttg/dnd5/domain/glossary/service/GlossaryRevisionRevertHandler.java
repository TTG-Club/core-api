package club.ttg.dnd5.domain.glossary.service;

import club.ttg.dnd5.domain.glossary.rest.dto.create.GlossaryRequest;
import club.ttg.dnd5.domain.revision.service.RevisionRevertHandler;
import club.ttg.dnd5.exception.ApiException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GlossaryRevisionRevertHandler implements RevisionRevertHandler {

    private final GlossaryService glossaryService;
    private final ObjectMapper objectMapper;

    @Override
    public String entityType() {
        return GlossaryService.REVISION_ENTITY_TYPE;
    }

    @Override
    public void revert(String entityId, String snapshotJson) {
        GlossaryRequest request;
        try {
            request = objectMapper.readValue(snapshotJson, GlossaryRequest.class);
        } catch (JsonProcessingException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Не удалось прочитать снимок глоссария для отката");
        }
        glossaryService.update(entityId, request);
    }
}
