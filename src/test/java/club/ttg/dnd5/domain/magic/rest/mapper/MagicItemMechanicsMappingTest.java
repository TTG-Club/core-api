package club.ttg.dnd5.domain.magic.rest.mapper;

import club.ttg.dnd5.domain.common.model.ActiveEffect;
import club.ttg.dnd5.domain.magic.model.MagicItem;
import club.ttg.dnd5.domain.magic.model.mechanics.MagicItemActivation;
import club.ttg.dnd5.domain.magic.model.mechanics.MagicItemMechanics;
import club.ttg.dnd5.domain.magic.model.mechanics.MagicItemRechargeEvent;
import club.ttg.dnd5.domain.magic.model.mechanics.MagicItemResource;
import club.ttg.dnd5.domain.magic.rest.dto.MagicItemRequest;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class MagicItemMechanicsMappingTest {

    @Mock
    private BaseMapping baseMapping;

    @InjectMocks
    private MagicItemMapperImpl mapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void rawFormReturnsMechanicsOfEntity() {
        MagicItem entity = new MagicItem();
        entity.setMechanics(cloakOfProtection());

        MagicItemRequest request = mapper.toRequest(entity);
        List<ActiveEffect.Change> changes = request.getMechanics()
                .getActiveEffects()
                .getFirst()
                .getChanges();

        assertEquals(MagicItemActivation.WORN, request.getMechanics().getActivation());
        assertEquals(2, changes.size());
        assertEquals("armorClass", changes.get(0).getKey());
        assertEquals("save.constitution", changes.get(1).getKey());
    }

    @Test
    void updateWithoutMechanicsClearsThem() {
        MagicItem entity = new MagicItem();
        entity.setMechanics(cloakOfProtection());

        mapper.updateEntity(new MagicItemRequest(), null, Set.of(), entity);

        assertNull(entity.getMechanics());
    }

    /**
     * Снимок ревизии — это сериализованный {@link MagicItemRequest}, а откат читает его
     * обратно. Значит механика обязана переживать оба конца JSON без потерь.
     */
    @Test
    void revisionSnapshotSurvivesJsonRoundTrip() throws Exception {
        MagicItemRequest request = new MagicItemRequest();
        request.setMechanics(situationalWithCharges());

        String snapshot = objectMapper.writeValueAsString(request);
        MagicItemMechanics restored = objectMapper.readValue(snapshot, MagicItemRequest.class).getMechanics();
        ActiveEffect.Change change = restored.getActiveEffects().getFirst().getChanges().getFirst();

        assertEquals(MagicItemActivation.HELD, restored.getActivation());
        assertEquals("add", change.getMode());
        assertEquals("attacker.isRanged === true", change.getCondition());
        assertEquals(Integer.valueOf(7), restored.getResource().getMaxCharges());
        assertEquals(MagicItemRechargeEvent.DAWN, restored.getResource().getRechargeEvent());
        assertEquals("1к6+1", restored.getResource().getRecharge());
        assertEquals("Вы можете дышать под водой", restored.getPassive());
    }

    /** Плащ защиты: надет, +1 к КД и +1 ко всем спасброскам. */
    private MagicItemMechanics cloakOfProtection() {
        ActiveEffect effect = new ActiveEffect();
        effect.setName("Плащ защиты");
        effect.setChanges(List.of(
                change("armorClass", "1", null),
                change("save.constitution", "1", null)));

        MagicItemMechanics mechanics = new MagicItemMechanics();
        mechanics.setActivation(MagicItemActivation.WORN);
        mechanics.setActiveEffects(List.of(effect));
        return mechanics;
    }

    /** Предмет с зарядами и ситуационным «+2 КД против дальнобойных атак». */
    private MagicItemMechanics situationalWithCharges() {
        ActiveEffect effect = new ActiveEffect();
        effect.setChanges(List.of(change("armorClass", "2", "attacker.isRanged === true")));

        MagicItemResource resource = new MagicItemResource();
        resource.setMaxCharges(7);
        resource.setRecharge("1к6+1");
        resource.setRechargeEvent(MagicItemRechargeEvent.DAWN);
        resource.setCost(1);

        MagicItemMechanics mechanics = new MagicItemMechanics();
        mechanics.setActivation(MagicItemActivation.HELD);
        mechanics.setActiveEffects(List.of(effect));
        mechanics.setResource(resource);
        mechanics.setPassive("Вы можете дышать под водой");
        return mechanics;
    }

    private ActiveEffect.Change change(String key, String value, String condition) {
        ActiveEffect.Change change = new ActiveEffect.Change();
        change.setKey(key);
        change.setMode("add");
        change.setValue(value);
        change.setCondition(condition);
        return change;
    }
}
