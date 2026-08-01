package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.source.model.Source;
import org.springframework.util.StringUtils;

import java.util.Locale;

/**
 * Ключ источника в вокабуляре VTTG ({@code sourceKey} записи компендиума).
 *
 * <p>Правило одно на всю выгрузку намеренно: по этому же ключу к записи подбирается
 * подпись из словаря источников, который выгрузка отдаёт отдельно
 * ({@code VttgChangesResponse#sources}). Разойдись эти два места — записи остались бы
 * без названия источника, поэтому и ключ записи, и ключ словаря считаются здесь.</p>
 */
final class VttgSourceKeys {
    /** Ключ, когда источник не указан. */
    static final String FALLBACK = "srd";

    private VttgSourceKeys() {
    }

    /**
     * Ключ источника: аббревиатура в нижнем регистре.
     *
     * <p>{@code PHB24} сводится к {@code phb}: в TTG Club это отдельный источник, а в
     * вокабуляре VTTG «Книга игрока» одна.</p>
     *
     * @param source источник сущности; {@code null} — источник не задан
     */
    static String of(Source source) {
        if (source == null || !StringUtils.hasText(source.getAcronym())) {
            return FALLBACK;
        }
        if ("PHB24".equalsIgnoreCase(source.getAcronym())) {
            return "phb";
        }
        return source.getAcronym().toLowerCase(Locale.ROOT);
    }
}
