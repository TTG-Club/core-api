package club.ttg.dnd5.domain.full_text_search.rest;

import club.ttg.dnd5.domain.full_text_search.rest.dto.FullTextSearchViewResponse;
import club.ttg.dnd5.domain.full_text_search.service.FullTextSearchViewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Tag(name = "Поиск по всем разделам", description = "API для поиска по всем разделам")
@RequestMapping("/api/v2/full-text-search")
@RestController
public class FullTextSearchController {

    private final FullTextSearchViewService fullTextSearchViewService;

    @Operation(summary = "Поиск по всем разделам")
    @GetMapping
    public FullTextSearchViewResponse search(@RequestParam(name = "query")
                                                 @Valid
                                                 @Size(min = 2)
                                                 @Schema(description = "Строка поиска")
                                                 String searchLine) {
        return fullTextSearchViewService.findBySearchLine(searchLine);
    }

}
