package club.ttg.dnd5.domain.beastiary.rest.mapper;

import club.ttg.dnd5.domain.beastiary.model.CreatureTrait;
import club.ttg.dnd5.domain.beastiary.model.action.CooldownTurns;
import club.ttg.dnd5.domain.beastiary.rest.dto.TraitRequest;
import club.ttg.dnd5.domain.beastiary.rest.dto.TraitResponse;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TraitMapper {
    @Mapping(source = "name.name", target = "name")
    @Mapping(source = "name.english", target = "english")
    CreatureTrait toEntity(TraitRequest request);

    @Mapping(source = "name.name", target = "name")
    @Mapping(source = "name.english", target = "english")
    CreatureTrait toEntity(TraitResponse response);

    // Entity → Response DTO
    @Mapping(source = "name", target = "name.name")
    @Mapping(source = "english", target = "name.english")
    TraitResponse toResponse(CreatureTrait trait);

    // Entity → Request DTO (если нужно)
    @Mapping(source = "name", target = "name.name")
    @Mapping(source = "english", target = "name.english")
    TraitRequest toRequest(CreatureTrait trait);

    /**
     * Не даёт сохранить «раз в N ходов», которое система не примет.
     *
     * Вне 2–20 число ходов система отбросит, а рядом с перезарядкой оно не действует:
     * система берёт перезарядку. Храни мы такое значение, форма показывала бы
     * ограничение, которого в игре нет.
     *
     * @param request запрос из формы.
     * @param trait собранная MapStruct черта, которая уходит в базу.
     */
    @AfterMapping
    default void dropInapplicableCooldown(TraitRequest request, @MappingTarget CreatureTrait trait) {
        trait.setCooldownTurns(CooldownTurns.resolve(trait.getRecharge(), trait.getCooldownTurns()));
    }
}
