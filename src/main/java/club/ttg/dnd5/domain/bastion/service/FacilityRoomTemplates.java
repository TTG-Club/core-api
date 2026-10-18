package club.ttg.dnd5.domain.bastion.service;

import club.ttg.dnd5.domain.bastion.model.FacilityRoomTemplate;
import club.ttg.dnd5.domain.bastion.model.FacilitySpace;
import club.ttg.dnd5.exception.ApiException;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpStatus;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Проверка шаблонов готовой комнаты сооружения: по одному на пространство, прямоугольник ровно
 * площадью пространства, мебель целиком внутри и без наложений.
 */
@UtilityClass
public class FacilityRoomTemplates {
    private static final Set<Integer> ROTATIONS = Set.of(0, 90, 180, 270);

    /**
     * Шаблон для пространства сооружения.
     *
     * @param templates шаблоны сооружения
     * @param space     пространство
     * @return шаблон или {@code null}, если для пространства его нет
     */
    public static FacilityRoomTemplate forSpace(List<FacilityRoomTemplate> templates, FacilitySpace space) {
        return Optional.ofNullable(templates).orElse(List.of()).stream()
                .filter(template -> template.getSpace() == space)
                .findFirst()
                .orElse(null);
    }

    /**
     * @param templates шаблоны из формы сооружения
     * @throws ApiException 400 с объяснением
     */
    public static void validate(List<FacilityRoomTemplate> templates) {
        if (templates == null) {
            return;
        }
        Set<FacilitySpace> spaces = EnumSet.noneOf(FacilitySpace.class);
        for (FacilityRoomTemplate template : templates) {
            if (template == null || template.getSpace() == null) {
                throw badRequest("У шаблона комнаты не указано пространство");
            }
            if (!spaces.add(template.getSpace())) {
                throw badRequest("Шаблон комнаты для пространства «%s» указан дважды"
                        .formatted(template.getSpace().getName()));
            }
            if (template.getWidth() < 1 || template.getDepth() < 1
                    || template.getWidth() * template.getDepth() != template.getSpace().getSquares()) {
                throw badRequest("Комната пространства «%s» должна занимать ровно %d клеток"
                        .formatted(template.getSpace().getName(), template.getSpace().getSquares()));
            }
            validateFurniture(template);
        }
    }

    private static void validateFurniture(FacilityRoomTemplate template) {
        Set<Long> taken = new HashSet<>();
        for (FacilityRoomTemplate.Item item : Optional.ofNullable(template.getFurniture()).orElse(List.of())) {
            if (item == null || item.getKind() == null || !ROTATIONS.contains(item.getRotation())) {
                throw badRequest("В шаблоне комнаты предмет без вида или с неверным поворотом");
            }
            boolean turned = item.getRotation() % 180 != 0;
            int width = turned ? item.getKind().depth() : item.getKind().width();
            int depth = turned ? item.getKind().width() : item.getKind().depth();
            if (item.getX() < 0 || item.getY() < 0
                    || item.getX() + width > template.getWidth() || item.getY() + depth > template.getDepth()) {
                throw badRequest("Предмет шаблона комнаты выходит за её стены");
            }
            for (int y = item.getY(); y < item.getY() + depth; y++) {
                for (int x = item.getX(); x < item.getX() + width; x++) {
                    if (!taken.add(((long) x << 32) | y)) {
                        throw badRequest("Предметы шаблона комнаты стоят друг на друге");
                    }
                }
            }
        }
    }

    private static ApiException badRequest(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, message);
    }
}
