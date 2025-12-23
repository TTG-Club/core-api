package club.ttg.dnd5.domain.update.rest.controller;

import club.ttg.dnd5.domain.full_text_search.model.FullTextSearchView;
import club.ttg.dnd5.domain.full_text_search.repository.FullTextSearchViewRepository;
import club.ttg.dnd5.domain.update.rest.dto.LastUpdate;
import club.ttg.dnd5.domain.update.rest.mapper.LastUpdateMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@Tag(name = "Последние обновления", description = "API для отображения последних обновлений")
@RequestMapping("/api/v2/last/update")
@RestController
public class LastUpdateController {
    private final FullTextSearchViewRepository fullTextSearchViewRepository;
    private final LastUpdateMapper lastUpdateMapper;

    @GetMapping
    public List<LastUpdate> getLastUpdates(int top) {
        List<FullTextSearchView> top10 = fullTextSearchViewRepository.findTop10LatestUpdatedOrCreated(
                PageRequest.of(0, top));
        return top10.stream()
                .map(lastUpdateMapper::toResponse)
                .toList();
    }
}
