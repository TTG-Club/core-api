package club.ttg.dnd5.domain.feat.service;

import club.ttg.dnd5.domain.background.repository.BackgroundRepository;
import club.ttg.dnd5.domain.common.model.EntityRef;
import club.ttg.dnd5.domain.common.service.GrantedSpellResolver;
import club.ttg.dnd5.domain.feat.model.Feat;
import club.ttg.dnd5.domain.feat.model.mechanics.FeatMechanics;
import club.ttg.dnd5.domain.common.model.mechanics.GrantedSpellRef;
import club.ttg.dnd5.domain.common.model.mechanics.SpellGrant;
import club.ttg.dnd5.domain.common.model.mechanics.SpellListExpansion;
import club.ttg.dnd5.domain.common.model.mechanics.SpellListGroup;
import club.ttg.dnd5.domain.feat.repository.FeatRepository;
import club.ttg.dnd5.domain.feat.rest.dto.FeatDetailResponse;
import club.ttg.dnd5.domain.feat.rest.mapper.FeatMapper;
import club.ttg.dnd5.domain.revision.service.EntityRevisionService;
import club.ttg.dnd5.domain.source.service.SourceService;
import club.ttg.dnd5.domain.spell.model.Spell;
import club.ttg.dnd5.domain.spell.repository.SpellRepository;
import club.ttg.dnd5.domain.spell.rest.dto.SpellShortResponse;
import club.ttg.dnd5.domain.spell.rest.mapper.SpellMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Дополнение выдаваемых чертой заклинаний данными справочника: в механике они лежат
 * ссылками, а листу персонажа нужен круг, иначе заклинание некуда положить.
 */
@ExtendWith(MockitoExtension.class)
class FeatGrantedSpellsTest {

    @Mock
    private FeatRepository featRepository;

    @Mock
    private BackgroundRepository backgroundRepository;

    @Mock
    private FeatQueryDslSearchService featQueryDslSearchService;

    @Mock
    private SourceService sourceService;

    @Mock
    private FeatMapper featMapper;

    @Mock
    private SpellRepository spellRepository;

    @Mock
    private SpellMapper spellMapper;

    @Mock
    private EntityRevisionService revisionService;

    private FeatServiceImpl service;

    /**
     * Резолвер собирается настоящий, поверх замоканного справочника: проверяется путь
     * «ссылка механики → запись каталога» целиком, как он работает в приложении.
     */
    @BeforeEach
    void setUp() {
        service = new FeatServiceImpl(featRepository, backgroundRepository, featQueryDslSearchService,
                sourceService, featMapper, new GrantedSpellResolver(spellRepository, spellMapper),
                revisionService);
    }

    @Test
    void grantedSpellsAreResolvedFromCatalog() {
        stubFeat(grantOf("light-phb", "mending-phb"));

        Spell light = spellWithUrl("light-phb");
        Spell mending = spellWithUrl("mending-phb");

        when(spellRepository.findAllShortByUrlIn(any())).thenReturn(List.of(light, mending));
        when(spellMapper.toShort(light)).thenReturn(spellShort("light-phb"));
        when(spellMapper.toShort(mending)).thenReturn(spellShort("mending-phb"));

        var granted = service.getFeat("dragonmarked-phb").getGrantedSpells();

        // Порядок — как в механике: игрок видит заклинания так, как их перечислил редактор.
        assertIterableEquals(
                List.of("light-phb", "mending-phb"),
                granted.stream().map(entry -> entry.getSpell().getUrl()).toList());
    }

    /**
     * Механика — свободный JSONB, набранный руками: опечатка в url не должна ронять
     * страницу черты, поэтому ненайденное заклинание просто пропускается.
     */
    @Test
    void unknownSpellIsSkippedInsteadOfFailing() {
        stubFeat(grantOf("light-phb", "opechatka"));

        Spell light = spellWithUrl("light-phb");

        when(spellRepository.findAllShortByUrlIn(any())).thenReturn(List.of(light));
        when(spellMapper.toShort(light)).thenReturn(spellShort("light-phb"));

        var granted = service.getFeat("dragonmarked-phb").getGrantedSpells();

        assertIterableEquals(
                List.of("light-phb"),
                granted.stream().map(entry -> entry.getSpell().getUrl()).toList());
    }

    /** Черта заклинаний не выдаёт — поля в ответе нет вовсе. */
    @Test
    void featWithoutSpellsHasNoGrantedSpells() {
        stubFeat(new FeatMechanics());

        assertNull(service.getFeat("tough-phb").getGrantedSpells());
    }

    /** Механики может не быть вовсе — обращаться к её блокам нельзя. */
    @Test
    void featWithoutMechanicsHasNoGrantedSpells() {
        stubFeat(null);

        assertNull(service.getFeat("tough-phb").getGrantedSpells());
    }

    /**
     * Списки расширения едут со своим уровнем и количеством: странице и листу надо знать,
     * что вторая ступень открывается только на пятом уровне.
     */
    @Test
    void spellListGroupsKeepLevelAndCount() {
        stubFeat(spellListOf(
                spellListGroup(null, null, "identify-phb"),
                spellListGroup(5, "@prof", "fireball-phb")));

        Spell identify = spellWithUrl("identify-phb");
        Spell fireball = spellWithUrl("fireball-phb");
        when(spellRepository.findAllShortByUrlIn(any())).thenReturn(List.of(identify, fireball));
        when(spellMapper.toShort(identify)).thenReturn(spellShort("identify-phb"));
        when(spellMapper.toShort(fireball)).thenReturn(spellShort("fireball-phb"));

        var groups = List.copyOf(service.getFeat("mark-of-making-efa").getSpellListGroups());

        assertEquals(2, groups.size());
        assertNull(groups.get(0).getRequiredLevel());
        assertEquals(5, groups.get(1).getRequiredLevel());
        assertEquals("@prof", groups.get(1).getCount());
        assertIterableEquals(List.of("fireball-phb"),
                groups.get(1).getSpells().stream().map(SpellShortResponse::getUrl).toList());
    }

    /**
     * Плоское поле остаётся тем же по смыслу — все заклинания блока подряд, — но собирается
     * из списков: иначе у записи, где заклинания лежат только в них, таблица опустела бы.
     */
    @Test
    void flatSpellListSpellsAreCollectedFromGroups() {
        stubFeat(spellListOf(
                spellListGroup(null, null, "identify-phb"),
                spellListGroup(5, null, "fireball-phb", "identify-phb")));

        Spell identify = spellWithUrl("identify-phb");
        Spell fireball = spellWithUrl("fireball-phb");
        when(spellRepository.findAllShortByUrlIn(any())).thenReturn(List.of(identify, fireball));
        when(spellMapper.toShort(identify)).thenReturn(spellShort("identify-phb"));
        when(spellMapper.toShort(fireball)).thenReturn(spellShort("fireball-phb"));

        var flat = service.getFeat("mark-of-making-efa").getSpellListSpells();

        // Заклинание, стоящее в двух списках, в плоском поле только одно
        assertIterableEquals(List.of("identify-phb", "fireball-phb"),
                flat.stream().map(SpellShortResponse::getUrl).toList());
    }

    /** Запись прежней формы — плоский список без уровней — читается как один список. */
    @Test
    void flatSpellListIsReadAsSingleGroup() {
        SpellListExpansion expansion = new SpellListExpansion();
        expansion.setSpells(List.of(new EntityRef("identify-phb", null)));
        FeatMechanics mechanics = new FeatMechanics();
        mechanics.setSpellList(expansion);
        stubFeat(mechanics);

        Spell identify = spellWithUrl("identify-phb");
        when(spellRepository.findAllShortByUrlIn(any())).thenReturn(List.of(identify));
        when(spellMapper.toShort(identify)).thenReturn(spellShort("identify-phb"));

        var groups = List.copyOf(service.getFeat("mark-of-making-efa").getSpellListGroups());

        assertEquals(1, groups.size());
        assertNull(groups.get(0).getRequiredLevel());
        assertNull(groups.get(0).getCount());
        assertIterableEquals(List.of("identify-phb"),
                groups.get(0).getSpells().stream().map(SpellShortResponse::getUrl).toList());
    }

    /** Черта список не расширяет — обоих полей в ответе нет. */
    @Test
    void featWithoutSpellListHasNoGroups() {
        stubFeat(new FeatMechanics());

        var response = service.getFeat("tough-phb");

        assertNull(response.getSpellListGroups());
        assertNull(response.getSpellListSpells());
    }

    private static FeatMechanics spellListOf(SpellListGroup... groups) {
        SpellListExpansion expansion = new SpellListExpansion();
        expansion.setGroups(List.of(groups));

        FeatMechanics mechanics = new FeatMechanics();
        mechanics.setSpellList(expansion);
        return mechanics;
    }

    private static SpellListGroup spellListGroup(Integer requiredLevel, String count, String... urls) {
        SpellListGroup group = new SpellListGroup();
        group.setRequiredLevel(requiredLevel);
        group.setCount(count);
        group.setSpells(Arrays.stream(urls).map(url -> new EntityRef(url, null)).toList());
        return group;
    }

    private void stubFeat(FeatMechanics mechanics) {
        FeatDetailResponse response = new FeatDetailResponse();
        response.setMechanics(mechanics);

        when(featRepository.findById(anyString())).thenReturn(Optional.of(new Feat()));
        when(featMapper.toDetail(any())).thenReturn(response);
        when(backgroundRepository.findVisibleByFeatUrl(anyString())).thenReturn(List.of());
    }

    private static FeatMechanics grantOf(String... urls) {
        SpellGrant spells = new SpellGrant();
        spells.setSpells(Arrays.stream(urls).map(url -> new GrantedSpellRef(url, null, null)).toList());

        FeatMechanics mechanics = new FeatMechanics();
        mechanics.setSpells(spells);
        return mechanics;
    }

    private static Spell spellWithUrl(String url) {
        Spell spell = new Spell();
        spell.setUrl(url);
        return spell;
    }

    private static SpellShortResponse spellShort(String url) {
        SpellShortResponse response = new SpellShortResponse();
        response.setUrl(url);
        return response;
    }
}
