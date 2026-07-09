package club.ttg.dnd5.domain.spell.service;

import club.ttg.dnd5.domain.revision.service.RevisionRevertHandler;
import club.ttg.dnd5.domain.spell.rest.dto.create.SpellRequest;
import club.ttg.dnd5.exception.ApiException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Откат заклинаний: десериализует снимок в {@link SpellRequest} и применяет его
 * через обычный {@link SpellService#update}, который сам запишет новую версию.
 */
@Component
@RequiredArgsConstructor
public class SpellRevisionRevertHandler implements RevisionRevertHandler {

    private final SpellService spellService;
    private final ObjectMapper objectMapper;

    @Override
    public String entityType() {
        return SpellService.REVISION_ENTITY_TYPE;
    }

    @Override
    public void revert(String entityId, String snapshotJson) {
        SpellRequest request;
        try {
            request = objectMapper.readValue(snapshotJson, SpellRequest.class);
        } catch (JsonProcessingException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Не удалось прочитать снимок заклинания для отката");
        }
        spellService.update(entityId, request);
    }
}
