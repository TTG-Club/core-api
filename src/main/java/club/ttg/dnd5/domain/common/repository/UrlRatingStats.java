package club.ttg.dnd5.domain.common.repository;

/**
 * Рейтинг одной записи раздела в пакетной выборке: url записи и её статистика.
 */
public interface UrlRatingStats extends RatingStats {
    String getUrl();
}
