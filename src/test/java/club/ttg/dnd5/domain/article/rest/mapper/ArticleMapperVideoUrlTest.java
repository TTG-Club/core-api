package club.ttg.dnd5.domain.article.rest.mapper;

import club.ttg.dnd5.domain.article.model.Article;
import club.ttg.dnd5.domain.article.model.ArticleType;
import club.ttg.dnd5.domain.article.rest.dto.ArticleRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Ссылка на ролик проходит маппер статьи во все стороны: сущность, ответы и форма {@code /raw}. */
class ArticleMapperVideoUrlTest {

    private static final String VIDEO = "https://www.youtube.com/watch?v=dQw4w9WgXcQ&t=42s";

    private final ArticleMapper mapper = new ArticleMapperImpl();

    @Test
    void mapsVideoUrlEverywhere() {
        ArticleRequest request = new ArticleRequest();
        request.setUrl("news");
        request.setType(ArticleType.NEWS);
        request.setTitle("Новость");
        request.setVideoUrl(VIDEO);

        Article article = mapper.toEntity(request);
        assertEquals(VIDEO, article.getVideoUrl());
        assertEquals(VIDEO, mapper.toShortResponse(article).getVideoUrl());
        assertEquals(VIDEO, mapper.toDetailedResponse(article).getVideoUrl());
        assertEquals(VIDEO, mapper.toRequest(article).getVideoUrl());

        request.setVideoUrl("https://vk.com/video-1_2");
        mapper.updateEntity(article, request);
        assertEquals("https://vk.com/video-1_2", article.getVideoUrl());
    }
}
