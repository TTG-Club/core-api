package club.ttg.dnd5.domain.full_text_search.rest;

import club.ttg.dnd5.domain.common.rest.dto.select.DiceOptionDto;
import club.ttg.dnd5.domain.common.rest.dto.select.SelectOptionDto;
import club.ttg.dnd5.domain.common.service.DictionariesService;
import club.ttg.dnd5.domain.full_text_search.rest.dto.FullTextSearchViewResponse;
import club.ttg.dnd5.domain.full_text_search.service.FullTextSearchViewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RequiredArgsConstructor
@Tag(name = "Справочники", description = "API для различных справочников")
@RequestMapping("/api/v2/full-text-search")
@RestController
public class FullTextSearchController {

    private final FullTextSearchViewService fullTextSearchViewService;

    @Operation(summary = "Поиск по всем разделам")
    @PostMapping
    public Collection<FullTextSearchViewResponse> search(@RequestParam(name = "query")
                                                         @Valid
                                                         @Size(min = 3)
                                                         @Schema(description = "Строка поиска")
                                                         String searchLine) {
        return fullTextSearchViewService.findBySearchLine(searchLine);
    }

}
