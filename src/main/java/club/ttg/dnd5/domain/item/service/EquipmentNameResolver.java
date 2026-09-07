package club.ttg.dnd5.domain.item.service;

import club.ttg.dnd5.domain.common.rest.dto.EquipmentItemDto;
import club.ttg.dnd5.domain.common.rest.dto.EquipmentOptionDto;
import club.ttg.dnd5.domain.item.repository.ItemNameRef;
import club.ttg.dnd5.domain.item.repository.ItemRepository;
import club.ttg.dnd5.domain.magic.repository.MagicItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Подставляет в снаряжение актуальные названия предметов из справочника. В позиции
 * хранится ссылка на предмет и название на момент сохранения, и снимок мог устареть
 * после переименования. Название из снимка остаётся запасным — для предметов,
 * которых уже нет в справочнике.
 *
 * <p>Справочника два: обычные предметы и магические. Слаг у них общего вида, поэтому
 * магические спрашиваются вторым запросом и только про то, чего не нашлось в первом:
 * у стартового снаряжения предысторий и классов магических позиций не бывает, и
 * лишнего запроса там не случится.</p>
 */
@RequiredArgsConstructor
@Service
public class EquipmentNameResolver {

    private final ItemRepository itemRepository;
    private final MagicItemRepository magicItemRepository;

    /**
     * Названия в вариантах стартового снаряжения предыстории и класса.
     *
     * @param options варианты снаряжения.
     */
    @Transactional(readOnly = true)
    public void resolveNames(List<EquipmentOptionDto> options) {
        if (CollectionUtils.isEmpty(options)) {
            return;
        }

        resolveItemNames(options.stream()
                .map(EquipmentOptionDto::getItems)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .toList());
    }

    /**
     * Названия в плоском списке позиций — инвентарь существа лежит именно так.
     *
     * @param allItems позиции снаряжения.
     */
    @Transactional(readOnly = true)
    public void resolveItemNames(List<EquipmentItemDto> allItems) {
        if (CollectionUtils.isEmpty(allItems)) {
            return;
        }

        List<EquipmentItemDto> items = allItems.stream()
                .filter(Objects::nonNull)
                .filter(item -> StringUtils.hasText(item.getUrl()))
                .toList();

        if (items.isEmpty()) {
            return;
        }

        Set<String> urls = items.stream()
                .map(EquipmentItemDto::getUrl)
                .collect(Collectors.toSet());

        Map<String, String> names = new HashMap<>(names(itemRepository.findNamesByUrls(urls)));

        Set<String> missing = urls.stream()
                .filter(url -> !names.containsKey(url))
                .collect(Collectors.toSet());
        if (!missing.isEmpty()) {
            names.putAll(names(magicItemRepository.findNamesByUrls(missing)));
        }

        items.forEach(item -> item.setName(names.getOrDefault(item.getUrl(), item.getName())));
    }

    private Map<String, String> names(List<ItemNameRef> refs) {
        return refs.stream().collect(Collectors.toMap(ItemNameRef::getUrl, ItemNameRef::getName));
    }
}
