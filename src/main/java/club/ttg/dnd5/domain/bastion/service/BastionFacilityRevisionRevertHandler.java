package club.ttg.dnd5.domain.bastion.service;

import club.ttg.dnd5.domain.bastion.rest.dto.BastionFacilityRequest;
import club.ttg.dnd5.domain.revision.service.RevisionRevertHandler;
import club.ttg.dnd5.exception.ApiException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BastionFacilityRevisionRevertHandler implements RevisionRevertHandler {

    private final BastionFacilityService facilityService;
    private final ObjectMapper objectMapper;

    @Override
    public String entityType() {
        return BastionFacilityService.REVISION_ENTITY_TYPE;
    }

    @Override
    public void revert(String entityId, String snapshotJson) {
        BastionFacilityRequest request;
        try {
            request = objectMapper.readValue(snapshotJson, BastionFacilityRequest.class);
        } catch (JsonProcessingException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Не удалось прочитать снимок сооружения бастиона для отката");
        }
        facilityService.update(entityId, request);
    }
}
