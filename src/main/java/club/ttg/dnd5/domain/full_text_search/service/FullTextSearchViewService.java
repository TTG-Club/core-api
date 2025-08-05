package club.ttg.dnd5.domain.full_text_search.service;

import club.ttg.dnd5.domain.common.rest.dto.NameResponse;
import club.ttg.dnd5.domain.full_text_search.repository.FullTextSearchViewRepository;
import club.ttg.dnd5.domain.full_text_search.rest.dto.FullTextSearchViewDto;
import club.ttg.dnd5.domain.full_text_search.rest.dto.FullTextSearchViewResponse;
import club.ttg.dnd5.dto.base.SourceResponse;
import club.ttg.dnd5.util.SwitchLayoutUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FullTextSearchViewService {
    private final FullTextSearchViewRepository fullTextSearchViewRepository;

    //TODO доделать маппинг
    public FullTextSearchViewResponse findBySearchLine(String searchLine) {
        return Optional.ofNullable(searchLine)
                .filter(Predicate.not(String::isBlank))
                .map(line -> fullTextSearchViewRepository.findBySearchLine(line, SwitchLayoutUtils.switchLayout(line)))
                .map(results ->
                        FullTextSearchViewResponse.builder()
                                .result(results.stream().map(ftsv ->
                                        FullTextSearchViewDto.builder()
                                                .url(ftsv.getUrl())
                                                .name(NameResponse.builder()
                                                        .name(ftsv.getName())
                                                        .english(ftsv.getEnglish())
                                                        .build())
                                                .type(ftsv.getType())
                                                .source(SourceResponse.builder()
                                                        .name(NameResponse.builder()
                                                                .name(ftsv.getBookName())
                                                                .label(ftsv.getBookAcronym())
                                                                .english(ftsv.getBookEnglishName())
                                                                .build())
                                                        .group(NameResponse.builder()
                                                                .name(ftsv.getBookType().getGroup())
                                                                .label(ftsv.getBookType().getLabel())
                                                                .build())
                                                        .page(ftsv.getPage())
                                                        .build())
                                                .build()).collect(Collectors.toList()))
                                .total(results.size())
                                .build())
                .orElseGet(FullTextSearchViewResponse::new);
    }

}
