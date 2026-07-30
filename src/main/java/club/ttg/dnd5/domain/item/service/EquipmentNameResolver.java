package club.ttg.dnd5.domain.item.service;

import club.ttg.dnd5.domain.common.rest.dto.EquipmentItemDto;
import club.ttg.dnd5.domain.common.rest.dto.EquipmentOptionDto;
import club.ttg.dnd5.domain.item.repository.ItemNameRef;
import club.ttg.dnd5.domain.item.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Подставляет в стартовое снаряжение актуальные названия предметов из справочника.
 * В снаряжении хранится ссылка на предмет и название на момент сохранения, и снимок
 * мог устареть после переименования. Название из снимка остаётся запасным — для
 * предметов, которых уже нет в справочнике.
 */
@RequiredArgsConstructor
@Service
public class EquipmentNameResolver {

    private final ItemRepository itemRepository;

    @Transactional(readOnly = true)
    public void resolveNames(List<EquipmentOptionDto> options) {
        if (CollectionUtils.isEmpty(options)) {
            return;
        }

        List<EquipmentItemDto> items = options.stream()
                .map(EquipmentOptionDto::getItems)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .filter(item -> StringUtils.hasText(item.getUrl()))
                .toList();

        if (items.isEmpty()) {
            return;
        }

        Set<String> urls = items.stream()
                .map(EquipmentItemDto::getUrl)
                .collect(Collectors.toSet());

        Map<String, String> names = itemRepository.findNamesByUrls(urls).stream()
                .collect(Collectors.toMap(ItemNameRef::getUrl, ItemNameRef::getName));

        items.forEach(item -> item.setName(names.getOrDefault(item.getUrl(), item.getName())));
    }
}
