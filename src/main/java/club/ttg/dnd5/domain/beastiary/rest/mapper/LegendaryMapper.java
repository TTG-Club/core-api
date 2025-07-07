package club.ttg.dnd5.domain.beastiary.rest.mapper;

import club.ttg.dnd5.domain.beastiary.model.Creature;
import club.ttg.dnd5.domain.beastiary.rest.dto.LegendaryActionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", uses = { ActionMapper.class })
public abstract class LegendaryMapper {
    @Autowired
    protected ActionMapper actionMapper;

    @Named("toLegendary")
    public LegendaryActionResponse toLegendary(Creature creature) {
        var response = new LegendaryActionResponse();

        if (creature.getLegendaryActions() != null) {
            response.setActions(
                    creature.getLegendaryActions().stream()
                            .map(actionMapper::toResponse)
                            .toList()
            );
        }

        var inLair = creature.getLegendaryActionInLair();
        var base = creature.getLegendaryAction();

        response.setCount(inLair > 0
                ? "%d (%d в логове)".formatted(base, inLair)
                : String.valueOf(base)
        );
        response.setDescription(creature.getLegendaryDescription());
        return response;
    }
}
