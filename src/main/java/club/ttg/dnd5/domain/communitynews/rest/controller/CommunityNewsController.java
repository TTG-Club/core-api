package club.ttg.dnd5.domain.communitynews.rest.controller;

import club.ttg.dnd5.domain.communitynews.model.CommunityNewsRoles;
import club.ttg.dnd5.domain.communitynews.rest.dto.CommunityNewsDetailedResponse;
import club.ttg.dnd5.domain.communitynews.rest.dto.CommunityNewsRequest;
import club.ttg.dnd5.domain.communitynews.rest.dto.CommunityNewsShortResponse;
import club.ttg.dnd5.domain.communitynews.service.CommunityNewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * «Новости сообщества» — отдельный инструмент со своими правами (см. {@link CommunityNewsRoles}).
 * Публикация как у статей; кросс-постинга в соцсети, тегов, рейтинга и ревизий нет.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v2/community-news")
@Tag(name = "Новости сообщества", description = "REST API новостей сообщества")
public class CommunityNewsController {

    private final CommunityNewsService service;

    @Operation(summary = "Проверка занятости url (для админки при выборе slug): "
            + "200 — url уже занят (в т.ч. черновиком/удалённой/отложенной), 404 — свободен")
    @CommunityNewsRoles.Editors
    @RequestMapping(path = "/{url}", method = RequestMethod.HEAD)
    public boolean existByUrl(@PathVariable final String url) {
        service.validateUrlExistence(url);
        return true;
    }

    @Operation(summary = "Полная новость сообщества по url: активная и вышедшая либо открытая по прямой ссылке")
    @GetMapping("/{url}")
    public CommunityNewsDetailedResponse findByUrl(@PathVariable final String url) {
        return service.findByUrl(url);
    }

    @Operation(summary = "Форма для редактирования новости сообщества по url (то же, что тело запроса)")
    @CommunityNewsRoles.Editors
    @GetMapping("/{url}/raw")
    public CommunityNewsRequest getRawByUrl(@PathVariable final String url) {
        return service.findFormByUrl(url);
    }

    @Operation(summary = "Создание новости сообщества; в ответе — url")
    @CommunityNewsRoles.Editors
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public String create(@RequestBody @Valid final CommunityNewsRequest request) {
        return service.save(request);
    }

    @Operation(summary = "Обновление новости сообщества; в ответе — url (мог смениться)")
    @CommunityNewsRoles.Editors
    @PutMapping("/{url}")
    public String update(@PathVariable final String url, @RequestBody @Valid final CommunityNewsRequest request) {
        return service.update(url, request);
    }

    @Operation(summary = "Предпросмотр новости сообщества без сохранения")
    @CommunityNewsRoles.Editors
    @PostMapping("/preview")
    public CommunityNewsDetailedResponse preview(@RequestBody @Valid final CommunityNewsRequest request) {
        return service.preview(request);
    }

    @Operation(summary = "Удаление новости сообщества (мягкое: запись скрывается, url остаётся занятым)")
    @CommunityNewsRoles.Editors
    @DeleteMapping("/{url}")
    public String delete(@PathVariable final String url) {
        return service.delete(url);
    }

    @Operation(summary = "Последние вышедшие новости сообщества, свежие сверху (с поиском по заголовку)")
    @GetMapping("/search")
    public List<CommunityNewsShortResponse> search(
            @RequestParam(required = false)
            @Schema(description = "Сколько новостей грузить (по умолчанию — 10)") final Integer cnt,
            @RequestParam(required = false)
            @Schema(description = "Поиск по подстроке в заголовке (регистронезависимо)") final String search) {
        return service.searchPublished(cnt, search);
    }

    @Operation(summary = "Неопубликованные новости сообщества: черновики, снятые и запланированные одним списком")
    @CommunityNewsRoles.Editors
    @GetMapping("/search/unpublished")
    public List<CommunityNewsShortResponse> searchUnpublished(
            @RequestParam(required = false)
            @Schema(description = "Сколько новостей грузить (по умолчанию — 10)") final Integer cnt,
            @RequestParam(required = false)
            @Schema(description = "Поиск по подстроке в заголовке (регистронезависимо)") final String search) {
        return service.searchUnpublished(cnt, search);
    }
}
