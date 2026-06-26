package club.ttg.dnd5.domain.magic.service;

import club.ttg.dnd5.domain.magic.rest.dto.MagicItemRequest;
import club.ttg.dnd5.domain.revision.service.RevisionRevertHandler;
import club.ttg.dnd5.exception.ApiException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MagicItemRevisionRevertHandler implements RevisionRevertHandler {

    private final MagicItemService magicItemService;
    private final ObjectMapper objectMapper;

    @Override
    public String entityType() {
        return MagicItemServiceImpl.REVISION_ENTITY_TYPE;
    }

    @Override
    public void revert(String entityId, String snapshotJson) {
        MagicItemRequest request;
        try {
            request = objectMapper.readValue(snapshotJson, MagicItemRequest.class);
        } catch (JsonProcessingException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Не удалось прочитать снимок магического предмета для отката");
        }
        magicItemService.updateItem(entityId, request);
    }
}
