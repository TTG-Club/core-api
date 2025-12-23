package club.ttg.dnd5.domain.full_text_search.service;

import club.ttg.dnd5.domain.common.rest.dto.NameResponse;
import club.ttg.dnd5.domain.full_text_search.model.FullTextSearchView;
import club.ttg.dnd5.domain.common.model.SectionType;
import club.ttg.dnd5.domain.full_text_search.repository.FullTextSearchViewRepository;
import club.ttg.dnd5.domain.full_text_search.rest.dto.FullTextSearchViewDto;
import club.ttg.dnd5.domain.full_text_search.rest.dto.FullTextSearchViewResponse;
import club.ttg.dnd5.dto.base.SourceResponse;
import club.ttg.dnd5.util.SwitchLayoutUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FullTextSearchViewService {
    private final FullTextSearchViewRepository fullTextSearchViewRepository;

    @Value("${global-search.limit-per-group:5}")
    private int maxItemsPerGroup;

    //TODO доделать маппинг
    public FullTextSearchViewResponse findBySearchLine(String searchLine) {
        return Optional.ofNullable(searchLine)
                .map(String::trim)
                .filter(Predicate.not(String::isBlank))
                .map(line -> fullTextSearchViewRepository.findBySearchLine(line, SwitchLayoutUtils.switchLayout(line)))
                .map(this::getFullTextSearchViewResponse)
                .orElseGet(this::getEmptyResponse);
    }

    private FullTextSearchViewResponse getFullTextSearchViewResponse(Collection<FullTextSearchView> results) {
        if (results.isEmpty()) {
            return getEmptyResponse();
        }

        Map<SectionType, Integer> typeCount = new HashMap<>();

        List<FullTextSearchViewDto> filtered = results.parallelStream()
                .filter(ftsv -> counterFilter(ftsv, typeCount))
                .map(this::getConvertedResult)
                .collect(Collectors.toList());

        return FullTextSearchViewResponse.builder()
                .items(filtered)
                .filtered(filtered.size())
                .total(results.size())
                .build();
    }

    private boolean counterFilter(FullTextSearchView item, Map<SectionType, Integer> typeCount) {
        SectionType type = item.getType();
        int count = typeCount.getOrDefault(type, 0);
        if (count < maxItemsPerGroup) {
            typeCount.put(type, count + 1);
            return true;
        }
        return false;
    }

    private FullTextSearchViewDto getConvertedResult(FullTextSearchView ftsv) {
        return FullTextSearchViewDto.builder()
                .url(ftsv.getUrl())
                .name(NameResponse.builder()
                        .name(ftsv.getName())
                        .english(ftsv.getEnglish())
                        .build())
                .type(ftsv.getType())
                .source(SourceResponse.builder()
                        .name(NameResponse.builder()
                                .name(ftsv.getSourceName())
                                .label(ftsv.getAcronym())
                                .english(ftsv.getSourceEnglish())
                                .build())
                        .group(NameResponse.builder()
                                .name(ftsv.getSourceType().getGroup())
                                .label(ftsv.getSourceType().getLabel())
                                .build())
                        .page(ftsv.getPage())
                        .build())
                .build();
    }

    private FullTextSearchViewResponse getEmptyResponse() {
        return FullTextSearchViewResponse.builder()
                .items(Collections.emptyList())
                .filtered(0)
                .total(0)
                .build();
    }

}
