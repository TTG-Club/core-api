package club.ttg.dnd5.domain.feat.rest.mapper;

import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.model.EntityRef;
import club.ttg.dnd5.domain.feat.model.Feat;
import club.ttg.dnd5.domain.feat.model.mechanics.FeatMechanics;
import club.ttg.dnd5.domain.common.model.mechanics.GrantedSpellRef;
import club.ttg.dnd5.domain.common.model.mechanics.SpellGrant;
import club.ttg.dnd5.domain.common.model.mechanics.SpellListExpansion;
import club.ttg.dnd5.domain.common.model.mechanics.SpellListGroup;
import club.ttg.dnd5.domain.feat.rest.dto.FeatDetailResponse;
import club.ttg.dnd5.domain.feat.rest.dto.FeatRequest;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class FeatSpellGrantMappingTest {

    @Mock
    private BaseMapping baseMapping;

    @InjectMocks
    private FeatMapperImpl mapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void detailExposesGrantedSpells() {
        Feat feat = new Feat();
        feat.setMechanics(dragonmarked());

        FeatDetailResponse response = mapper.toDetail(feat);
        SpellGrant spells = response.getMechanics().getSpells();

        assertEquals(2, spells.getSpells().size());
        assertEquals("light-phb", spells.getSpells().getFirst().getUrl());
        assertEquals(Ability.CHARISMA, spells.getSpellcastingAbility());
    }

    @Test
    void rawFormKeepsGrantOfEntity() {
        Feat feat = new Feat();
        feat.setMechanics(dragonmarked());

        FeatRequest request = mapper.toRequest(feat);

        assertTrue(request.getMechanics().getSpells().getAlwaysPrepared());
    }

    /**
     * Ссылками, а не кругом со школой: те берутся из записи справочника и здесь
     * разошлись бы с каталогом при правке заклинания.
     */
    @Test
    void spellsSerializeAsCatalogRefs() throws Exception {
        SpellGrant spells = new SpellGrant();
        spells.setSpells(List.of(new GrantedSpellRef("fly-phb", "Полёт", null)));

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(spells));

        assertEquals("fly-phb", json.get("spells").get(0).get("url").asText());
        assertEquals("Полёт", json.get("spells").get(0).get("name").asText());
        assertFalse(json.has("level"));
        assertFalse(json.has("school"));
    }

    /** Незаполненные поля в JSON не уходят — как и у прочих блоков механики. */
    @Test
    void emptyFieldsAreOmitted() throws Exception {
        SpellGrant spells = new SpellGrant();
        spells.setSpells(List.of(new GrantedSpellRef("fly-phb", "Полёт", null)));

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(spells));

        assertFalse(json.has("spellcastingAbility"));
        assertFalse(json.has("alwaysPrepared"));
    }

    /**
     * Уровень выдачи едет вместе со ссылкой и плоско: у метки дракона «Малое
     * восстановление» приходит на третьем уровне, и без уровня лист выдал бы его сразу.
     */
    @Test
    void grantedSpellKeepsRequiredLevel() throws Exception {
        SpellGrant spells = new SpellGrant();
        spells.setSpells(List.of(
                new GrantedSpellRef("cure-wounds-phb", "Лечение ран", null),
                new GrantedSpellRef("lesser-restoration-phb", "Малое восстановление", 3)));

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(spells));

        // Доступное сразу поля не несёт: пустой уровень и читается как «с момента взятия»
        assertFalse(json.get("spells").get(0).has("requiredLevel"));
        assertEquals(3, json.get("spells").get(1).get("requiredLevel").asInt());
        assertEquals("lesser-restoration-phb", json.get("spells").get(1).get("url").asText());
    }

    /**
     * Ссылка, сохранённая до появления уровня, читается без правок — иначе поле нельзя
     * было бы завести, не мигрируя весь JSONB.
     */
    @Test
    void grantWithoutLevelStaysReadable() throws Exception {
        SpellGrant spells = objectMapper.readValue(
                "{\"spells\":[{\"url\":\"fly-phb\",\"name\":\"Полёт\"}]}", SpellGrant.class);

        assertEquals("fly-phb", spells.getSpells().getFirst().getUrl());
        assertNull(spells.getSpells().getFirst().getRequiredLevel());
    }

    /**
     * Расширение списка заклинаний — отдельный блок: такое заклинание игрок не знает, а
     * лишь может подготовить наравне с классовыми.
     */
    @Test
    void spellListIsSeparateFromGrant() {
        SpellListExpansion expansion = new SpellListExpansion();
        expansion.setSpells(List.of(new EntityRef("identify-phb", "Опознание")));
        expansion.setRequiresSpellcasting(true);

        FeatMechanics mechanics = new FeatMechanics();
        mechanics.setSpellList(expansion);
        Feat feat = new Feat();
        feat.setMechanics(mechanics);

        FeatDetailResponse response = mapper.toDetail(feat);

        assertNull(response.getMechanics().getSpells());
        assertEquals("identify-phb",
                response.getMechanics().getSpellList().getSpells().getFirst().getUrl());
        assertTrue(response.getMechanics().getSpellList().getRequiresSpellcasting());
    }

    /** Круг в расширении не хранится: он свойство записи и разошёлся бы с каталогом. */
    @Test
    void spellListDoesNotSnapshotSpellLevel() throws Exception {
        SpellListExpansion expansion = new SpellListExpansion();
        expansion.setSpells(List.of(new EntityRef("identify-phb", "Опознание")));

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(expansion));

        assertFalse(json.get("spells").get(0).has("level"));
        assertFalse(json.has("requiresSpellcasting"));
    }

    /**
     * Списки расширения открываются ступенями: у каждого свой уровень доступа и своё
     * количество. Без этого таблица метки открывалась бы целиком на первом уровне.
     */
    @Test
    void spellListGroupKeepsLevelAndCount() throws Exception {
        SpellListGroup group = new SpellListGroup();
        group.setRequiredLevel(5);
        group.setCount("@prof");
        group.setSpells(List.of(new EntityRef("identify-phb", "Опознание")));

        SpellListExpansion expansion = new SpellListExpansion();
        expansion.setGroups(List.of(group));

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(expansion));
        JsonNode first = json.get("groups").get(0);

        assertEquals(5, first.get("requiredLevel").asInt());
        assertEquals("@prof", first.get("count").asText());
        assertEquals("identify-phb", first.get("spells").get(0).get("url").asText());
        // Доступный сразу и целиком список полей не несёт — как и прочие пустые поля
        assertFalse(json.has("spells"));
    }

    /**
     * Запись, сохранённая до появления списков, читается как один список — доступный сразу
     * и целиком. Иначе заводить поле было бы нельзя, не мигрируя весь JSONB.
     */
    @Test
    void flatSpellListIsResolvedAsSingleGroup() throws Exception {
        SpellListExpansion expansion = objectMapper.readValue(
                "{\"spells\":[{\"url\":\"identify-phb\"}],\"requiresSpellcasting\":true}",
                SpellListExpansion.class);

        var groups = expansion.resolveGroups();

        assertEquals(1, groups.size());
        assertNull(groups.getFirst().getRequiredLevel());
        assertNull(groups.getFirst().getCount());
        assertEquals("identify-phb", groups.getFirst().getSpells().getFirst().getUrl());
    }

    /** Заполненные списки старое плоское поле перебивают: две формы одного блока не смешиваются. */
    @Test
    void groupsWinOverFlatSpells() {
        SpellListGroup group = new SpellListGroup();
        group.setSpells(List.of(new EntityRef("fireball-phb", null)));

        SpellListExpansion expansion = new SpellListExpansion();
        expansion.setGroups(List.of(group));
        expansion.setSpells(List.of(new EntityRef("identify-phb", null)));

        var groups = expansion.resolveGroups();

        assertEquals(1, groups.size());
        assertEquals("fireball-phb", groups.getFirst().getSpells().getFirst().getUrl());
    }

    /** Пустой блок списков не создаёт — расширять нечем. */
    @Test
    void emptySpellListResolvesToNoGroups() {
        assertTrue(new SpellListExpansion().resolveGroups().isEmpty());
    }

    /** Служебное поле разбора в JSON не уходит — оно не часть контракта. */
    @Test
    void resolvedGroupsAreNotSerialized() throws Exception {
        SpellListExpansion expansion = new SpellListExpansion();
        expansion.setSpells(List.of(new EntityRef("identify-phb", null)));

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(expansion));

        assertFalse(json.has("resolvedGroups"));
        assertFalse(json.has("groups"));
    }

    /** Заклинаний у черты может не быть вовсе — блока тогда нет. */
    @Test
    void mechanicsWithoutSpellsKeepsFieldNull() {
        Feat feat = new Feat();
        feat.setMechanics(new FeatMechanics());

        FeatDetailResponse response = mapper.toDetail(feat);

        assertNull(response.getMechanics().getSpells());
    }

    /** «Отмеченный драконом»: свой набор заклинаний, всегда подготовленных. */
    private static FeatMechanics dragonmarked() {
        SpellGrant spells = new SpellGrant();
        spells.setSpells(
                List.of(new GrantedSpellRef("light-phb", "Свет", null),
                        new GrantedSpellRef("mending-phb", "Починка", null)));
        spells.setSpellcastingAbility(Ability.CHARISMA);
        spells.setAlwaysPrepared(true);

        FeatMechanics mechanics = new FeatMechanics();
        mechanics.setSpells(spells);
        return mechanics;
    }
}
