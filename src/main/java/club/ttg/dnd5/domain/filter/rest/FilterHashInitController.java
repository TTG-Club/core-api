package club.ttg.dnd5.domain.filter.rest;

import club.ttg.dnd5.domain.beastiary.repository.CreatureRepository;
import club.ttg.dnd5.domain.character_class.model.CharacterClass;
import club.ttg.dnd5.domain.character_class.service.ClassService;
import club.ttg.dnd5.domain.filter.model.FilterHashCategory;
import club.ttg.dnd5.domain.filter.service.FilterHashService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Tag(name = "Инициализация фильтров", description = "Утилиты для администраторов по инициализации хэш-маппингов фильтров")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/filters/hashes")
public class FilterHashInitController
{
    private final FilterHashService hashService;
    private final CreatureRepository creatureRepository;
    private final ClassService classService;

    @Secured("ADMIN")
    @Operation(summary = "Массовая инициализация хэшей", description = "Генерирует короткие хэши для существующих тегов и классов, записывая их в БД")
    @PostMapping("/init")
    public Map<String, Integer> initAllHashes()
    {
        Map<String, Integer> results = new LinkedHashMap<>();

        // 1. Инициализация тегов бестиария (Tag)
        try {
            List<String> tags = creatureRepository.findDistinctTags();
            // Разделяем по запятой, т.к. теги могут приходить объединенными
            List<String> splitTags = tags.stream()
                    .filter(t -> t != null && !t.isBlank())
                    .flatMap(t -> java.util.Arrays.stream(t.split(",")))
                    .map(String::trim)
                    .distinct()
                    .toList();

            int addedTags = hashService.initHashes(FilterHashCategory.TAG, splitTags);
            results.put("TAG", addedTags);
            log.info("Initialized {} hashes for TAG", addedTags);
        } catch (Exception e) {
            log.error("Failed to init TAG hashes", e);
            results.put("TAG_ERROR", 0);
        }

        // 2. Инициализация URL классов и подклассов
        try {
            List<String> classUrls = new ArrayList<>();
            classService.findAllMagicClasses().stream().map(CharacterClass::getUrl).forEach(classUrls::add);
            classService.findAllMagicSubclasses().stream().map(CharacterClass::getUrl).forEach(classUrls::add);

            // Также нужно добавить базовые не-магические классы как возможные источники? 
            // Заклинания обычно привязаны ко всем классам через getUrl().
            
            int addedClasses = hashService.initHashes(FilterHashCategory.CLASS_URL, classUrls);
            results.put("CLASS_URL", addedClasses);
            log.info("Initialized {} hashes for CLASS_URL", addedClasses);
        } catch (Exception e) {
            log.error("Failed to init CLASS_URL hashes", e);
            results.put("CLASS_URL_ERROR", 0);
        }

        return results;
    }
}
