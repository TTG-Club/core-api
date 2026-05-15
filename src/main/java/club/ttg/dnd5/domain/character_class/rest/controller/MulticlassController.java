package club.ttg.dnd5.domain.character_class.rest.controller;

import club.ttg.dnd5.domain.character_class.rest.dto.MulticlassResponse;
import club.ttg.dnd5.domain.character_class.service.MulticlassService;
import club.ttg.dnd5.domain.common.rest.dto.MulticlassLevelEntry;
import club.ttg.dnd5.domain.common.rest.dto.MulticlassRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/multiclass")
@Tag(name = "Мультиклассирование", description = "API для управления мультиклассами")
public class MulticlassController {
    private static final Pattern CLASS_QUERY_PARAM = Pattern.compile("class(\\d+)");

    private final MulticlassService multiclassService;

    @GetMapping
    public MulticlassResponse getClassByQuery(@RequestParam MultiValueMap<String, String> query) {
        return multiclassService.getMulticlass(toRequest(query));
    }

    @PostMapping
    public MulticlassResponse getClassByUrl(@RequestBody MulticlassRequest request) {
        return multiclassService.getMulticlass(request);
    }

    private MulticlassRequest toRequest(MultiValueMap<String, String> query) {
        List<MulticlassLevelEntry> levels = query.keySet()
                .stream()
                .map(CLASS_QUERY_PARAM::matcher)
                .filter(Matcher::matches)
                .map(matcher -> Integer.parseInt(matcher.group(1)))
                .sorted(Comparator.naturalOrder())
                .map(index -> toLevelEntry(query, index))
                .toList();

        MulticlassRequest request = new MulticlassRequest();
        request.setLevels(levels);
        return request;
    }

    private MulticlassLevelEntry toLevelEntry(MultiValueMap<String, String> query, int index) {
        String classUrl = query.getFirst("class" + index);
        String rawLevel = query.getFirst("level" + index);

        if (rawLevel == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing level" + index);
        }

        try {
            return new MulticlassLevelEntry(
                    classUrl,
                    query.getFirst("subclass" + index),
                    Integer.parseInt(rawLevel)
            );
        } catch (NumberFormatException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid level" + index, exception);
        }
    }
}
