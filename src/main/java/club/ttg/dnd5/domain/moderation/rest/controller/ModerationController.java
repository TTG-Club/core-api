package club.ttg.dnd5.domain.moderation.rest.controller;

import club.ttg.dnd5.domain.moderation.rest.dto.ModerationRequest;
import club.ttg.dnd5.domain.moderation.rest.dto.ModerationResponse;
import club.ttg.dnd5.domain.moderation.rest.dto.ModerationShortResponse;
import club.ttg.dnd5.domain.moderation.service.ModerationService;
import club.ttg.dnd5.domain.common.model.SectionType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Админ панель", description = "REST API админ панели")
@RestController
@RequestMapping("/api/v2/moderation")
@RequiredArgsConstructor
public class ModerationController {

    private final ModerationService moderationService;

    @Operation(summary = "Получение созданных страниц для администрирования")
    @GetMapping("/pages")
    public List<ModerationResponse> getPageList(@RequestParam ModerationRequest request, Pageable pageable) {
        return moderationService.getAllPages(request.getSectionTypes(), request.getStatusTypes(), pageable);
    }

    @Operation(summary = "Получение количества созданных страниц для администрирования")
    @GetMapping("/count")
    public List<Pair<SectionType, Integer>> getCountPage(@RequestParam ModerationRequest request) {
        return moderationService.getPageCount(request.getStatusTypes());
    }

    @Operation(summary = "Изменение статуса страницы")
    @PostMapping("/update")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void update(@PathVariable String url, @RequestParam ModerationRequest request) {
        moderationService.update(request, url);
    }

    @Operation(summary = "Получение админ информации по странице")
    @PostMapping("/move")
    public ModerationShortResponse getPageStatus(@PathVariable String url) {
        return moderationService.getPageStatus(url);
    }
}
