package club.ttg.dnd5.domain.full_text_search.service;

import club.ttg.dnd5.domain.common.rest.dto.NameResponse;
import club.ttg.dnd5.domain.full_text_search.repository.FullTextSearchViewRepository;
import club.ttg.dnd5.domain.full_text_search.rest.dto.FullTextSearchViewDto;
import club.ttg.dnd5.domain.full_text_search.rest.dto.FullTextSearchViewResponse;
import club.ttg.dnd5.dto.base.SourceResponse;
import club.ttg.dnd5.util.SwitchLayoutUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FullTextSearchViewService {
    private final FullTextSearchViewRepository fullTextSearchViewRepository;

    //TODO доделать маппинг
    public FullTextSearchViewResponse findBySearchLine(String searchLine) {
        Pageable pageable = PageRequest.of(0, 5);
        return Optional.ofNullable(searchLine)
                .filter(Predicate.not(String::isBlank))
                .map(line -> fullTextSearchViewRepository.findBySearchLine(line, SwitchLayoutUtils.switchLayout(line), pageable))
                .map(page ->
                        FullTextSearchViewResponse.builder()
                                .result(page.getContent().stream().map(ftsv ->
                                        FullTextSearchViewDto.builder()
                                                .url(ftsv.getUrl())
                                                .name(NameResponse.builder()
                                                        .name(ftsv.getName())
                                                        .english(ftsv.getEnglish())
                                                        .build())
                                                .type(ftsv.getType())
                                                .source(SourceResponse.builder()
                                                        .name(NameResponse.builder()
                                                                .name(ftsv.getName())
                                                                .label(ftsv.getBookAcronym())
                                                                .english(ftsv.getEnglish())
                                                                .build())
                                                        .group(NameResponse.builder()
                                                                .name(ftsv.getBookType().getName())
                                                                .label(ftsv.getBookType().getGroup())
                                                                .build())
                                                        .page(ftsv.getPage())
                                                        .build())
                                                .build()).collect(Collectors.toList()))
                                .total(page.getTotalElements())
                                .build())
                .orElseGet(FullTextSearchViewResponse::new);
    }

}
