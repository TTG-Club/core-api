package club.ttg.dnd5.domain.beastiary.rest.mapper;

import club.ttg.dnd5.domain.beastiary.model.Creature;
import club.ttg.dnd5.domain.beastiary.model.spellcasting.CreatureSpellComponents;
import club.ttg.dnd5.domain.beastiary.model.spellcasting.CreatureSpellGroup;
import club.ttg.dnd5.domain.beastiary.model.spellcasting.CreatureSpellRef;
import club.ttg.dnd5.domain.beastiary.model.spellcasting.CreatureSpellRestKind;
import club.ttg.dnd5.domain.beastiary.model.spellcasting.CreatureSpellUsageMode;
import club.ttg.dnd5.domain.beastiary.model.spellcasting.CreatureSpellcastingBlock;
import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureRequest;
import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.RechargeType;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

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

    /**
     * Круг «форма открыла запись и сохранила»: {@code /raw} отдаёт {@link CreatureRequest},
     * а сохранение переписывает JSONB целиком — блок, которого нет в запросе, после захода
     * в форму стал бы {@code null}.
     *
     * <p>Ключ блока держится отдельно: его генерирует форма, и по нему виртуальный стол
     * держится за блок при переименовании. Бэкенд его не выдумывает и не переписывает.</p>
     */
    @Test
    void formKeepsSpellcastingBlocksThroughSaveCycle() {
        CreatureSpellRef ray = new CreatureSpellRef();
        ray.setUrl("ray-of-sickness-phb");
        ray.setName("Луч болезни");
        ray.setCastLevel(3);
        ray.setNote("только на себя");

        CreatureSpellGroup pool = new CreatureSpellGroup();
        pool.setMode(CreatureSpellUsageMode.PER_REST_POOL);
        pool.setCount(2);
        pool.setRest(CreatureSpellRestKind.SHORT);
        pool.setRecharge(RechargeType.D5);
        pool.setLabel("1/день");
        pool.setSpells(List.of(ray));

        CreatureSpellComponents ignored = new CreatureSpellComponents();
        ignored.setMaterial(true);

        CreatureSpellcastingBlock block = new CreatureSpellcastingBlock();
        block.setId("spellcasting-4d0a4a2e-6c8f-4a1e-9a2b-1f7c2d3e4f50");
        block.setName("Использование заклинаний");
        block.setNote("находясь в пределах 30 футов от двух союзных карг");
        block.setAbility(Ability.WISDOM);
        block.setSaveDc(12);
        block.setAttackBonus(4);
        block.setIgnoredComponents(ignored);
        block.setGroups(List.of(pool, new CreatureSpellGroup()));

        Creature creature = new Creature();
        creature.setSpellcasting(List.of(block));

        Creature saved = new Creature();
        mapper.updateEntity(mapper.toRequest(creature), null, saved);

        assertEquals(creature.getSpellcasting(), saved.getSpellcasting());
        assertEquals("spellcasting-4d0a4a2e-6c8f-4a1e-9a2b-1f7c2d3e4f50",
                saved.getSpellcasting().getFirst().getId());
    }

    /** Существо без блоков не заводит их само: пустой список — это не «блок есть». */
    @Test
    void formLeavesSpellcastingEmptyWhenItWasNotFilled() {
        Creature creature = new Creature();

        assertNull(mapper.toRequest(creature).getSpellcasting());
    }
}
