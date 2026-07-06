package club.ttg.dnd5.domain.article.service;

import club.ttg.dnd5.domain.article.rest.dto.ArticleRequest;
import club.ttg.dnd5.domain.revision.service.RevisionRevertHandler;
import club.ttg.dnd5.exception.ApiException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ArticleRevisionRevertHandler implements RevisionRevertHandler {

    private final ArticleService articleService;
    private final ObjectMapper objectMapper;

    @Override
    public String entityType() {
        return ArticleService.REVISION_ENTITY_TYPE;
    }

    @Override
    public void revert(String entityId, String snapshotJson) {
        ArticleRequest request;
        try {
            request = objectMapper.readValue(snapshotJson, ArticleRequest.class);
        } catch (JsonProcessingException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Не удалось прочитать снимок новости для отката");
        }
        articleService.update(UUID.fromString(entityId), request);
    }
}
