package club.ttg.dnd5.domain.item.service;

import club.ttg.dnd5.domain.item.rest.dto.ItemRequest;
import club.ttg.dnd5.domain.revision.service.RevisionRevertHandler;
import club.ttg.dnd5.exception.ApiException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ItemRevisionRevertHandler implements RevisionRevertHandler {

    private final ItemService itemService;
    private final ObjectMapper objectMapper;

    @Override
    public String entityType() {
        return ItemServiceImpl.REVISION_ENTITY_TYPE;
    }

    @Override
    public void revert(String entityId, String snapshotJson) {
        ItemRequest request;
        try {
            request = objectMapper.readValue(snapshotJson, ItemRequest.class);
        } catch (JsonProcessingException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Не удалось прочитать снимок предмета для отката");
        }
        itemService.updateItem(entityId, request);
    }
}
