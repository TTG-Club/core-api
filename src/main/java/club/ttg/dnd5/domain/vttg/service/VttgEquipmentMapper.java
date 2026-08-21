package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.common.dictionary.Coin;
import club.ttg.dnd5.domain.common.model.EquipmentItem;
import club.ttg.dnd5.domain.common.model.EquipmentOption;
import club.ttg.dnd5.domain.common.model.SectionType;
import club.ttg.dnd5.domain.common.rest.mapper.EquipmentMapping;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgEquipmentItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Стартовое снаряжение вариантами выбора → формат компендиума VTTG.
 *
 * <p>Такое снаряжение есть у предысторий и классов, а в VTTG у них разные DTO
 * ({@code VttgBackground.EquipmentOption} с золотой альтернативой против
 * {@code VttgClass.StartingEquipment} с меткой варианта). Общее — как из структуры
 * собирается видимая строка, поэтому здесь живёт именно она, а раскладка по полям
 * остаётся за мапперами сущностей.</p>
 *
 * <p>Предметы уезжают ссылками на карточки сайта: раздел «Снаряжение» в VTTG
 * отрисовывается разбором markdown, поэтому ссылка ведёт туда же, куда маркер
 * {@code {@item ...|url:...}} из свободного текста.</p>
 */
@Component
@RequiredArgsConstructor
public class VttgEquipmentMapper {

    private final VttgMarkupConverter markupConverter;

    /**
     * Развёрнутый вариант снаряжения.
     *
     * @param description     видимая строка варианта
     * @param goldEquivalent  золотой эквивалент для варианта «только монеты»; {@code null},
     *                        если в варианте есть предметы — тогда это не альтернатива деньгами
     * @param items           позиции варианта — по ним потребитель кладёт предметы в инвентарь;
     *                        пусто, если в варианте только монеты
     * @param coins           количество монет варианта; {@code null}, если монет нет
     * @param coin            код монеты ({@code GC}, {@code SC}, …); {@code null} без монет
     */
    public record RenderedOption(String description, Integer goldEquivalent,
                                 List<VttgEquipmentItem> items, Integer coins, String coin) {
    }

    /**
     * Разворачивает варианты снаряжения; пустые (без предметов и без монет) отбрасываются.
     * Порядок сохраняется — из него выводятся метки «А», «Б», … (см. {@link #label(int)}).
     *
     * @param options варианты из модели ({@code startingEquipment})
     */
    public List<RenderedOption> render(List<EquipmentOption> options) {
        if (CollectionUtils.isEmpty(options)) {
            return List.of();
        }
        return options.stream()
                .filter(Objects::nonNull)
                .map(this::renderOption)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Метка варианта по его порядку — «А», «Б», «В»… Берётся из общего с сайтом набора,
     * чтобы в VTTG варианты назывались так же, как в карточке на ttg.club.
     *
     * @param index порядковый номер варианта, с нуля
     */
    public String label(int index) {
        return index < EquipmentMapping.EQUIPMENT_LABELS.length()
                ? String.valueOf(EquipmentMapping.EQUIPMENT_LABELS.charAt(index))
                : String.valueOf(index + 1);
    }

    /**
     * Вариант → строка: предметы через запятую, монеты последней позицией. Вариант без
     * предметов и без монет отбрасывается ({@code null}).
     */
    private RenderedOption renderOption(EquipmentOption option) {
        List<EquipmentItem> items = option.getItems() == null ? List.of() : option.getItems();
        Coin coin = option.getCoin() == null ? Coin.GC : option.getCoin();
        Integer coins = option.getCoins();
        boolean hasCoins = coins != null && coins > 0;

        List<String> parts = new ArrayList<>();
        List<VttgEquipmentItem> exported = new ArrayList<>();
        for (EquipmentItem item : items) {
            String part = renderItem(item);
            if (StringUtils.hasText(part)) {
                parts.add(part);
            }
            VttgEquipmentItem entry = exportItem(item);
            if (entry != null) {
                exported.add(entry);
            }
        }
        if (hasCoins) {
            parts.add(coins + " " + coin.getShortName());
        }
        if (parts.isEmpty()) {
            return null;
        }

        Integer goldEquivalent = parts.size() == 1 && hasCoins
                ? Math.round(coins * coin.getExchangeForGold())
                : null;
        return new RenderedOption(String.join(", ", parts), goldEquivalent,
                exported.isEmpty() ? null : exported,
                hasCoins ? coins : null,
                hasCoins ? coin.name() : null);
    }

    /**
     * Позиция варианта для автовыдачи. Уточнение отдаётся отдельным полем, а не внутри
     * названия: потребитель ищет предмет по слагу, а уточнение показывает игроку.
     * Позиция без названия — пустая строка источника, её отбрасываем.
     */
    private VttgEquipmentItem exportItem(EquipmentItem item) {
        String name = StringUtils.hasText(item.getName()) ? item.getName().trim() : null;
        if (name == null) {
            return null;
        }
        Integer quantity = item.getQuantity();
        String note = StringUtils.hasText(item.getDescription())
                ? markupConverter.toText(item.getDescription()).trim()
                : null;
        return new VttgEquipmentItem(
                StringUtils.hasText(item.getUrl()) ? item.getUrl() : null,
                name,
                quantity != null && quantity > 1 ? quantity : null,
                StringUtils.hasText(note) ? note : null);
    }

    /**
     * Позиция варианта: {@code «2 [Кинжал](ссылка) (по вашему выбору)»}. Позиция без
     * названия — это свободное уточнение вроде «древние карты»: карточки за ним нет,
     * поэтому оно идёт голым текстом, без скобок.
     */
    private String renderItem(EquipmentItem item) {
        String note = StringUtils.hasText(item.getDescription())
                ? markupConverter.toText(item.getDescription()).trim()
                : null;
        String name = StringUtils.hasText(item.getName()) ? item.getName().trim() : null;
        if (name == null) {
            return note == null ? "" : note;
        }

        Integer quantity = item.getQuantity();
        String prefix = quantity != null && quantity > 1 ? quantity + " " : "";
        String link = markupConverter.siteLink(SectionType.ITEM.getValue(), item.getUrl(), name);
        return note == null ? prefix + link : prefix + link + " (" + note + ")";
    }
}
