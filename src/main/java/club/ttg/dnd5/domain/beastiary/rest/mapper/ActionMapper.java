package club.ttg.dnd5.domain.beastiary.rest.mapper;

import club.ttg.dnd5.domain.beastiary.model.action.CreatureAction;
import club.ttg.dnd5.domain.beastiary.model.action.CreatureActionEffect;
import club.ttg.dnd5.domain.beastiary.rest.dto.ActionRequest;
import club.ttg.dnd5.domain.beastiary.rest.dto.ActionResponse;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.util.CollectionUtils;

import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface ActionMapper {
    @Mapping(source = "name.name", target = "name")
    @Mapping(source = "name.english", target = "english")
    CreatureAction toEntity(ActionRequest request);

    @Mapping(source = "name.name", target = "name")
    @Mapping(source = "name.english", target = "english")
    CreatureAction toEntity(ActionResponse response);

    // Entity → Response DTO
    @Mapping(source = "name", target = "name.name")
    @Mapping(source = "english", target = "name.english")
    ActionResponse toResponse(CreatureAction action);

    // Entity → Request DTO (если нужно)
    @Mapping(source = "name", target = "name.name")
    @Mapping(source = "english", target = "name.english")
    ActionRequest toRequest(CreatureAction action);

    /**
     * Поднимает поля старого импорта в механику запроса.
     *
     * Форма читает запись через `/raw`, а сохранение переписывает JSONB целиком:
     * не подними мы тип атаки, спасброски и типы урона в `effect`, круг «открыл
     * форму — сохранил» стёр бы их у каждой записи старого импорта. Заодно
     * редактор видит, какая механика в записи уже была.
     *
     * @param action запись существа из базы.
     * @param request собранный MapStruct запрос, который уходит в форму.
     */
    @AfterMapping
    default void raiseLegacyMechanics(CreatureAction action, @MappingTarget ActionRequest request) {
        boolean hasLegacy = action.getAttackType() != null
                || !CollectionUtils.isEmpty(action.getSawingThrows())
                || !CollectionUtils.isEmpty(action.getDamageTypes());

        if (!hasLegacy) {
            return;
        }

        CreatureActionEffect effect = request.getEffect() == null
                ? new CreatureActionEffect()
                : request.getEffect();

        if (effect.getAttackType() == null) {
            effect.setAttackType(action.getAttackType());
        }

        if (CollectionUtils.isEmpty(effect.getSavingThrows())) {
            effect.setSavingThrows(action.getSawingThrows());
        }

        if (CollectionUtils.isEmpty(effect.getDamageTypes())) {
            effect.setDamageTypes(action.getDamageTypes());
        }

        request.setEffect(effect);
    }

    /**
     * Возвращает поля старого импорта на их места в сущности.
     *
     * Зеркало {@link #raiseLegacyMechanics}: эти поля кормят разбор описания
     * регулярками у записей, которым механику ещё не завели, и потеряться в
     * круге через форму не должны.
     *
     * @param request запрос из формы.
     * @param action собранная MapStruct запись, которая уходит в базу.
     */
    @AfterMapping
    default void lowerLegacyMechanics(ActionRequest request, @MappingTarget CreatureAction action) {
        CreatureActionEffect effect = request.getEffect();

        if (effect == null) {
            return;
        }

        action.setAttackType(effect.getAttackType());
        action.setSawingThrows(effect.getSavingThrows());
        action.setDamageTypes(effect.getDamageTypes());
    }
}