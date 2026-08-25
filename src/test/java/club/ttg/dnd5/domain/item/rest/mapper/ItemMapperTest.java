package club.ttg.dnd5.domain.item.rest.mapper;

import club.ttg.dnd5.domain.common.model.ActiveEffect;
import club.ttg.dnd5.domain.item.model.Item;
import club.ttg.dnd5.domain.item.model.tool.Tool;
import club.ttg.dnd5.domain.item.model.weapon.DamagePart;
import club.ttg.dnd5.domain.item.model.weapon.Weapon;
import club.ttg.dnd5.domain.item.rest.dto.ItemRequest;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Round-trip блоков предмета, появившихся ради паритета с системой D&amp;D приложения.
 *
 * <p>Форма мастерской и снимки ревизий собираются одним {@code toRequest}, поэтому
 * потеря поля здесь означала бы и пустую вкладку в редакторе, и испорченную историю
 * правок.</p>
 */
@ExtendWith(MockitoExtension.class)
class ItemMapperTest {

    @Mock
    private BaseMapping baseMapping;

    @InjectMocks
    private ItemMapperImpl mapper;

    /** Части урона и боевые поля оружия доезжают до записи и обратно в форму. */
    @Test
    void keepsWeaponParityFieldsBetweenRequestAndEntity() {
        Weapon weapon = new Weapon();
        weapon.setBaseType("longsword");
        weapon.setReach(10);
        weapon.setAttackAbility("dexterity");
        weapon.setProficiencyMode("always");
        weapon.setAttackBonus(1);
        weapon.setDamageAbility("none");
        weapon.setDamageBonus(2);
        weapon.setSaveType("strength");
        weapon.setSaveEffect("half");
        weapon.setDamageParts(List.of(damagePart("1к8@dmg.slashing", "1к10@dmg.slashing")));

        ItemRequest request = new ItemRequest();
        request.setWeapon(weapon);

        Item item = mapper.toEntity(request, null);
        Weapon stored = item.getWeapon();

        assertNotNull(stored);
        assertEquals("longsword", stored.getBaseType());
        assertEquals(10, stored.getReach());
        assertEquals("dexterity", stored.getAttackAbility());
        assertEquals("always", stored.getProficiencyMode());
        assertEquals(1, stored.getAttackBonus());
        assertEquals("none", stored.getDamageAbility());
        assertEquals(2, stored.getDamageBonus());
        assertEquals("strength", stored.getSaveType());
        assertEquals("half", stored.getSaveEffect());

        Weapon roundTripped = mapper.toRequest(item).getWeapon();
        assertEquals(1, roundTripped.getDamageParts().size());
        assertEquals("1к8@dmg.slashing", roundTripped.getDamageParts().getFirst().getFormula());
        assertEquals("1к10@dmg.slashing",
                roundTripped.getDamageParts().getFirst().getVersatileFormula());
    }

    /** Инструмент и категория снаряжения — новые блоки, своих полей у записи не было. */
    @Test
    void keepsToolAndEquipmentCategoryBetweenRequestAndEntity() {
        Tool tool = new Tool();
        tool.setCategory("artisan");
        tool.setBaseType("calligraphers-supplies");
        tool.setAbility("intelligence");
        tool.setBonus(1);
        tool.setProficiencyMode("expertise");

        ItemRequest request = new ItemRequest();
        request.setTool(tool);
        request.setEquipmentCategory("clothing");

        Item item = mapper.toEntity(request, null);

        assertEquals("artisan", item.getTool().getCategory());
        assertEquals("calligraphers-supplies", item.getTool().getBaseType());
        assertEquals("intelligence", item.getTool().getAbility());
        assertEquals(1, item.getTool().getBonus());
        assertEquals("expertise", item.getTool().getProficiencyMode());
        assertEquals("clothing", item.getEquipmentCategory());

        ItemRequest roundTripped = mapper.toRequest(item);
        assertEquals("calligraphers-supplies", roundTripped.getTool().getBaseType());
        assertEquals("clothing", roundTripped.getEquipmentCategory());
    }

    /** Активные эффекты доезжают до записи и обратно — как у черты и заклинания. */
    @Test
    void keepsActiveEffectsBetweenRequestAndEntity() {
        ItemRequest request = new ItemRequest();
        request.setActiveEffects(List.of(effect("boots-of-speed")));

        Item item = mapper.toEntity(request, null);

        assertNotNull(item.getActiveEffects());
        assertEquals("boots-of-speed", item.getActiveEffects().getFirst().getId());
        assertEquals("boots-of-speed",
                mapper.toRequest(item).getActiveEffects().getFirst().getId());
    }

    /**
     * Пустой список стирает прежние эффекты: мастерская шлёт их целиком, и удалённый в
     * форме эффект обязан исчезнуть из записи, а не пережить сохранение.
     */
    @Test
    void emptyActiveEffectsClearStoredOnes() {
        Item item = new Item();
        item.setActiveEffects(new ArrayList<>(List.of(effect("boots-of-speed"))));

        ItemRequest request = new ItemRequest();
        request.setActiveEffects(List.of());

        mapper.updateEntity(request, null, item);

        assertTrue(item.getActiveEffects().isEmpty());
    }

    /**
     * Предмет без новых блоков сохраняется как раньше: девять сотен записей справочника
     * шлют форму без них, и появление пустышек ломало бы вывод категорий на столе.
     */
    @Test
    void leavesParityBlocksEmptyWhenRequestHasNone() {
        Item item = mapper.toEntity(new ItemRequest(), null);

        assertNull(item.getTool());
        assertNull(item.getEquipmentCategory());
        assertNull(item.getActiveEffects());
    }

    private static DamagePart damagePart(String formula, String versatileFormula) {
        DamagePart part = new DamagePart();
        part.setFormula(formula);
        part.setVersatileFormula(versatileFormula);
        return part;
    }

    private static ActiveEffect effect(String id) {
        ActiveEffect effect = new ActiveEffect();
        effect.setId(id);
        effect.setName("Скорость");
        return effect;
    }
}
