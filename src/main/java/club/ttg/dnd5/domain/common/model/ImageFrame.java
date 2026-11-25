package club.ttg.dnd5.domain.common.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Кадр обложки: какая часть основной картинки видна в квадрате страницы записи и в
 * карточке списка. Сам файл не обрезается — в галерее картинка показывается целиком.
 *
 * <p>Значения вне пределов не отклоняются, а прижимаются к ним: кадр — оформление, и
 * отказывать в сохранении записи из-за него незачем.</p>
 *
 * @param x    точка фокуса по горизонтали, доля ширины картинки, {@code 0..1}
 * @param y    точка фокуса по вертикали, доля высоты картинки, {@code 0..1}
 * @param zoom приближение, {@code 1..4}; 1 — картинка ровно заполняет рамку
 */
@Schema(description = "Кадр обложки: точка фокуса и приближение основной картинки")
public record ImageFrame(
        @Schema(description = "Точка фокуса по горизонтали, доля ширины", minimum = "0", maximum = "1", example = "0.42")
        double x,
        @Schema(description = "Точка фокуса по вертикали, доля высоты", minimum = "0", maximum = "1", example = "0.3")
        double y,
        @Schema(description = "Приближение", minimum = "1", maximum = "4", example = "1.8")
        double zoom
) {
    public static final double MIN_ZOOM = 1;
    public static final double MAX_ZOOM = 4;

    public ImageFrame {
        x = clamp(x, 0, 1, 0.5);
        y = clamp(y, 0, 1, 0.5);
        zoom = clamp(zoom, MIN_ZOOM, MAX_ZOOM, MIN_ZOOM);
    }

    private static double clamp(double value, double min, double max, double fallback) {
        if (Double.isNaN(value)) {
            return fallback;
        }

        return Math.max(min, Math.min(max, value));
    }
}
