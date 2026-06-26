package club.ttg.dnd5.domain.feat.service;

import club.ttg.dnd5.domain.feat.rest.dto.FeatRequest;
import club.ttg.dnd5.domain.revision.service.RevisionRevertHandler;
import club.ttg.dnd5.exception.ApiException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeatRevisionRevertHandler implements RevisionRevertHandler {

    private final FeatService featService;
    private final ObjectMapper objectMapper;

    @Override
    public String entityType() {
        return FeatServiceImpl.REVISION_ENTITY_TYPE;
    }

    @Override
    public void revert(String entityId, String snapshotJson) {
        FeatRequest request;
        try {
            request = objectMapper.readValue(snapshotJson, FeatRequest.class);
        } catch (JsonProcessingException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Не удалось прочитать снимок черты для отката");
        }
        featService.updateFeat(entityId, request);
    }
}
