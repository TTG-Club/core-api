package club.ttg.dnd5.domain.beastiary.rest.mapper;

import club.ttg.dnd5.domain.beastiary.model.Creature;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class CreatureMapperTest {

    @Mock
    private BaseMapping baseMapping;

    @InjectMocks
    private CreatureMapperImpl mapper;

    /**
     * Форма редактирования не должна смешивать тексты защиты: каждый берётся
     * из своего поля. Раньше текст иммунитетов подтягивался из уязвимостей,
     * и модератор сохранял в базу дубль.
     */
    @Test
    void formKeepsDefenceTextsSeparate() {
        Creature creature = new Creature();
        creature.setVulnerabilitiesText("колющий от существ под действием заклинания Благословение");
        creature.setResistanceText("от немагического оружия");
        creature.setImmunityText("от урона ядом при свете дня");

        var defenses = mapper.toRequest(creature).getDefenses();

        assertEquals("колющий от существ под действием заклинания Благословение",
                defenses.getVulnerabilities().getText());
        assertEquals("от немагического оружия", defenses.getResistances().getText());
        assertEquals("от урона ядом при свете дня", defenses.getImmunities().getText());
    }

    /**
     * Пустой текст иммунитетов остаётся пустым, даже когда у уязвимостей он есть.
     */
    @Test
    void formLeavesImmunityTextEmptyWhenOnlyVulnerabilityTextIsFilled() {
        Creature creature = new Creature();
        creature.setVulnerabilitiesText("колющий от существ под действием заклинания Благословение");

        var defenses = mapper.toRequest(creature).getDefenses();

        assertNull(defenses.getImmunities().getText());
    }
}
