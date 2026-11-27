package club.ttg.dnd5.domain.background.service;

import club.ttg.dnd5.domain.background.model.BackgroundToolChoice;
import club.ttg.dnd5.domain.background.rest.dto.BackgroundDetailResponse;
import club.ttg.dnd5.domain.common.model.EntityRef;
import club.ttg.dnd5.domain.feat.repository.FeatNameRef;
import club.ttg.dnd5.domain.feat.repository.FeatRepository;
import club.ttg.dnd5.domain.item.repository.ItemNameRef;
import club.ttg.dnd5.domain.item.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Подставляет в ссылки предыстории актуальные названия из справочника: инструменты
 * (фиксированные и пул выбора) — из предметов, черты на выбор — из черт.
 *
 * <p>Ссылка хранит url и снимок названия на момент сохранения, но снимок бывает пустым
 * (записи, заведённые миграцией) или устаревшим (после переименования). Без названия
 * лист персонажа не может показать вариант. Снимок остаётся запасным — для записей,
 * которых уже нет в справочнике. Так же поступает {@code EquipmentNameResolver} со
 * стартовым снаряжением.</p>
 *
 * <p>Ссылки пересоздаются, а не правятся на месте: элементы списка ответа могут быть
 * теми же объектами, что лежат в jsonb-поле сущности.</p>
 */
@RequiredArgsConstructor
@Service
public class BackgroundRefNameResolver {

    private final ItemRepository itemRepository;
    private final FeatRepository featRepository;

    /**
     * Названия во всех ссылках детали предыстории.
     *
     * @param response деталь предыстории.
     */
    @Transactional(readOnly = true)
    public void resolveNames(BackgroundDetailResponse response) {
        BackgroundToolChoice toolChoice = response.getToolChoice();
        List<EntityRef> choicePool = toolChoice == null ? null : toolChoice.getFrom();

        Map<String, String> toolNames = itemNames(Stream.concat(
                streamOf(response.getToolProficiencies()),
                streamOf(choicePool)));

        response.setToolProficiencies(rename(response.getToolProficiencies(), toolNames));

        if (toolChoice != null) {
            response.setToolChoice(new BackgroundToolChoice(toolChoice.getCount(), rename(choicePool, toolNames)));
        }

        Map<String, String> featNames = featNames(streamOf(response.getFeatChoices()));
        response.setFeatChoices(rename(response.getFeatChoices(), featNames));
    }

    private Map<String, String> itemNames(Stream<EntityRef> refs) {
        Set<String> urls = urlsOf(refs);

        if (urls.isEmpty()) {
            return Map.of();
        }

        return itemRepository.findNamesByUrls(urls).stream()
                .collect(Collectors.toMap(ItemNameRef::getUrl, ItemNameRef::getName, (first, second) -> first));
    }

    private Map<String, String> featNames(Stream<EntityRef> refs) {
        Set<String> urls = urlsOf(refs);

        if (urls.isEmpty()) {
            return Map.of();
        }

        return featRepository.findNamesByUrls(urls).stream()
                .collect(Collectors.toMap(FeatNameRef::getUrl, FeatNameRef::getName, (first, second) -> first));
    }

    private Set<String> urlsOf(Stream<EntityRef> refs) {
        return refs
                .map(EntityRef::getUrl)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
    }

    private Stream<EntityRef> streamOf(Collection<EntityRef> refs) {
        return CollectionUtils.isEmpty(refs) ? Stream.empty() : refs.stream().filter(Objects::nonNull);
    }

    private List<EntityRef> rename(List<EntityRef> refs, Map<String, String> names) {
        if (refs == null) {
            return null;
        }

        return refs.stream()
                .filter(Objects::nonNull)
                .map(ref -> new EntityRef(ref.getUrl(), names.getOrDefault(ref.getUrl(), ref.getName())))
                .toList();
    }
}
