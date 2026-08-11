package club.ttg.dnd5.domain.vttg.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

/**
 * Запись глоссария в формате компендиума VTTG ({@code type = "glossary"}).
 *
 * <p>Плоская справочная запись: название, категория тега и текст. Механики в ней нет —
 * в отличие от заклинаний и существ, поэтому и полей, кроме идентичности и описания, нет.</p>
 *
 * <p>Главная причина выгружать глоссарий — ссылки: описания заклинаний, черт, предысторий и
 * существ ссылаются на его записи ({@code {@glossary ...}}, см. {@link club.ttg.dnd5.domain.vttg.service.VttgMarkupConverter}).
 * Без записей в компендиуме такая ссылка в VTTG никуда не ведёт.</p>
 */
@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VttgGlossary {
    private String id;
    private String name;
    private String nameEn;
    /** Канонический тип сущности для VTTG — всегда "glossary". */
    private String type;
    /** Слаг листа дерева разделов, в котором показывается запись — всегда "glossary". */
    private String section;
    /**
     * Раздел сайта в адресе страницы-источника ({@code glossary} в {@code /glossary/action-phb}).
     * По паре {@code srcSection}/{@code srcUrl} VTTG находит запись в компендиуме, когда в
     * описании другой сущности кликают ссылку на термин.
     */
    private String srcSection;
    /** Слаг страницы-источника на сайте; с {@code srcSection} составляет адрес ссылки. */
    private String srcUrl;
    /** Ключ источника: "phb"/"dmg"/... */
    private String sourceKey;
    /** Категория тега записи («Действие», «Состояние», …) — по ней идёт группировка и фильтр. */
    private String category;
    private String description;
    /** Метка типа для отображения — всегда "Глоссарий". */
    private String typeLabel;

    @Getter(AccessLevel.NONE)
    private boolean isSRD;

    @JsonProperty("isSRD")
    public boolean isSRD() {
        return isSRD;
    }
}
