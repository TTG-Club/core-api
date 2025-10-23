package club.ttg.dnd5.domain.source.rest.controller;

import club.ttg.dnd5.domain.source.rest.dto.SourceDetailResponse;
import club.ttg.dnd5.domain.source.rest.dto.SourceRequest;
import club.ttg.dnd5.domain.source.service.SourceService;

import club.ttg.dnd5.domain.common.rest.dto.ShortResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/api/v2/books")
@RequiredArgsConstructor
@Tag(name = "Источники", description = "Контроллер для управления источниками и их поиском")
public class SourceController {
    private final SourceService sourceService;

    @Operation(summary = "Получить источник", description = "Возвращает детальную информацию об источнике")
    @GetMapping("/{url}")
    public SourceDetailResponse getByAcronym(@PathVariable String url) {
        return sourceService.findDetailByUrl(url);
    }

    @GetMapping("/{url}/raw")
    public SourceRequest getFormByUrl(@PathVariable String url) {
        return sourceService.findFormByUrl(url);
    }

    @Operation(summary = "Предпросмотр источника")
    @Secured("ADMIN")
    @PostMapping("/preview")
    public SourceDetailResponse preview(@RequestBody SourceRequest request) {
        return sourceService.preview(request);
    }

    /**
     * Получение всех источников.
     * @return список источников
     */
    @PostMapping("/search")
    @Operation(summary = "Получить источники", description = "Возвращает список источников")
    public Collection<ShortResponse> search(@RequestParam(name = "query", required = false)
                                                @Valid
                                                @Size(min = 2)
                                                @Schema(description = "Строка поиска, если null-отдаются все сущности")
                                                String searchLine) {
        return sourceService.search(searchLine);
    }

    @PostMapping
    @Operation(summary = "Добавить источник", description = "Добавление нового источника")
    public String create(SourceRequest request) {
        return sourceService.save(request);
    }

    @PutMapping
    @Operation(summary = "Обновить источник", description = "Обновление источника")
    public String update(SourceRequest request) {
        return sourceService.update(request);
    }
}
