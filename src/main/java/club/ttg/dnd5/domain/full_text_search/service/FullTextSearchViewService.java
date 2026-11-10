package club.ttg.dnd5.domain.full_text_search.service;

import club.ttg.dnd5.domain.common.rest.dto.NameResponse;
import club.ttg.dnd5.domain.full_text_search.model.FullTextSearchView;
import club.ttg.dnd5.domain.common.model.SectionType;
import club.ttg.dnd5.domain.full_text_search.repository.FullTextSearchViewRepository;
import club.ttg.dnd5.domain.full_text_search.rest.dto.FullTextSearchViewDto;
import club.ttg.dnd5.domain.full_text_search.rest.dto.FullTextSearchViewResponse;
import club.ttg.dnd5.dto.base.SourceResponse;
import club.ttg.dnd5.util.SwitchLayoutUtils;
import club.ttg.dnd5.util.YoUtils;
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

    private static final int EXACT_MATCH = 0;
    private static final int PREFIX_MATCH = 1;
    private static final int WORD_PREFIX_MATCH = 2;
    private static final int CONTAINS_MATCH = 3;
    private static final int NO_MATCH = 4;

    @Value("${global-search.limit-per-group:5}")
    private int maxItemsPerGroup;

    //TODO доделать маппинг
    public FullTextSearchViewResponse findBySearchLine(String searchLine) {
        return Optional.ofNullable(searchLine)
                .map(String::trim)
                .map(YoUtils::replaceYo)
                .filter(Predicate.not(String::isBlank))
                .map(this::search)
                .orElseGet(this::getEmptyResponse);
    }

    private FullTextSearchViewResponse search(String line) {
        String invertedLine = SwitchLayoutUtils.switchLayout(line);
        List<FullTextSearchView> results = fullTextSearchViewRepository.findBySearchLine(line, invertedLine);
        return getFullTextSearchViewResponse(results, List.of(line, invertedLine));
    }

    private FullTextSearchViewResponse getFullTextSearchViewResponse(List<FullTextSearchView> results,
                                                                     List<String> queries) {
        if (results.isEmpty()) {
            return getEmptyResponse();
        }

        Map<SectionType, Integer> typeCount = new HashMap<>();

        // Последовательно: лимит на группу должен брать первые N в порядке сортировки.
        List<FullTextSearchViewDto> filtered = results.stream()
                .sorted(byRelevance(queries))
                .filter(ftsv -> counterFilter(ftsv, typeCount))
                .map(this::getConvertedResult)
                .collect(Collectors.toList());

        return FullTextSearchViewResponse.builder()
                .items(filtered)
                .filtered(filtered.size())
                .total(results.size())
                .build();
    }

    /**
     * Сначала полное совпадение с названием, потом названия, начинающиеся с запроса,
     * потом запрос с начала слова внутри названия, потом любое вхождение. Среди равных —
     * короче название (ближе к запросу), затем по алфавиту, чтобы порядок не «плавал».
     */
    private static Comparator<FullTextSearchView> byRelevance(List<String> queries) {
        List<String> normalizedQueries = queries.stream()
                .map(FullTextSearchViewService::normalize)
                .toList();
        return Comparator.<FullTextSearchView>comparingInt(ftsv -> matchRank(ftsv, normalizedQueries))
                .thenComparingInt(ftsv -> ftsv.getName() == null ? Integer.MAX_VALUE : ftsv.getName().length())
                .thenComparing(FullTextSearchView::getName, Comparator.nullsLast(Comparator.naturalOrder()));
    }

    private static int matchRank(FullTextSearchView ftsv, List<String> queries) {
        int best = NO_MATCH;
        for (String field : new String[]{ftsv.getName(), ftsv.getEnglish(), ftsv.getAlternative()}) {
            if (field == null) {
                continue;
            }
            String normalizedField = normalize(field);
            for (String query : queries) {
                best = Math.min(best, matchRank(normalizedField, query));
            }
        }
        return best;
    }

    private static int matchRank(String field, String query) {
        if (query.isEmpty()) {
            return NO_MATCH;
        }
        if (field.equals(query)) {
            return EXACT_MATCH;
        }
        if (field.startsWith(query)) {
            return PREFIX_MATCH;
        }
        int index = field.indexOf(query);
        if (index < 0) {
            return NO_MATCH;
        }
        while (index >= 0) {
            if (!Character.isLetterOrDigit(field.charAt(index - 1))) {
                return WORD_PREFIX_MATCH;
            }
            index = field.indexOf(query, index + 1);
        }
        return CONTAINS_MATCH;
    }

    private static String normalize(String value) {
        return YoUtils.replaceYo(value).strip().toLowerCase(Locale.ROOT);
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
                .source(getSource(ftsv))
                .build();
    }

    /**
     * Источник (книга) есть не у всех разделов: у статей / новостей его нет,
     * тогда sourceType и page отсутствуют — возвращаем null, чтобы не разыменовывать
     * их (в т.ч. не распаковывать null Integer в примитивный page).
     */
    private SourceResponse getSource(FullTextSearchView ftsv) {
        if (ftsv.getSourceType() == null) {
            return null;
        }
        return SourceResponse.builder()
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
