package club.ttg.dnd5.domain.feat.service;

import club.ttg.dnd5.domain.background.repository.BackgroundRepository;
import club.ttg.dnd5.domain.common.model.EntityRef;
import club.ttg.dnd5.domain.feat.model.Feat;
import club.ttg.dnd5.domain.feat.model.mechanics.FeatMechanics;
import club.ttg.dnd5.domain.feat.model.mechanics.SpellGrant;
import club.ttg.dnd5.domain.feat.repository.FeatRepository;
import club.ttg.dnd5.domain.feat.rest.dto.FeatDetailResponse;
import club.ttg.dnd5.domain.feat.rest.mapper.FeatMapper;
import club.ttg.dnd5.domain.revision.service.EntityRevisionService;
import club.ttg.dnd5.domain.source.service.SourceService;
import club.ttg.dnd5.domain.spell.model.Spell;
import club.ttg.dnd5.domain.spell.repository.SpellRepository;
import club.ttg.dnd5.domain.spell.rest.dto.SpellShortResponse;
import club.ttg.dnd5.domain.spell.rest.mapper.SpellMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Дополнение выдаваемых чертой заклинаний данными справочника: в механике они лежат
 * ссылками, а листу персонажа нужен круг, иначе заклинание некуда положить.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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

    @InjectMocks
    private FeatServiceImpl service;

    @Test
    void grantedSpellsAreResolvedFromCatalog() {
        stubFeat(grantOf("light-phb", "mending-phb"));

        Spell light = spellWithUrl("light-phb");
        Spell mending = spellWithUrl("mending-phb");

        when(spellRepository.findAllShortByUrlIn(any())).thenReturn(List.of(light, mending));
        when(spellMapper.toShort(light)).thenReturn(shortOf("Свет"));
        when(spellMapper.toShort(mending)).thenReturn(shortOf("Починка"));

        var granted = service.getFeat("dragonmarked-phb").getGrantedSpells();

        assertEquals(2, granted.size());
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
        when(spellMapper.toShort(light)).thenReturn(shortOf("Свет"));

        var granted = service.getFeat("dragonmarked-phb").getGrantedSpells();

        assertEquals(1, granted.size());
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

    private void stubFeat(FeatMechanics mechanics) {
        FeatDetailResponse response = new FeatDetailResponse();
        response.setMechanics(mechanics);

        when(featRepository.findById(anyString())).thenReturn(Optional.of(new Feat()));
        when(featMapper.toDetail(any())).thenReturn(response);
        when(backgroundRepository.findVisibleByFeatUrl(anyString())).thenReturn(List.of());
    }

    private static FeatMechanics grantOf(String... urls) {
        SpellGrant spells = new SpellGrant();
        spells.setSpells(List.of(urls).stream().map(url -> new EntityRef(url, null)).toList());

        FeatMechanics mechanics = new FeatMechanics();
        mechanics.setSpells(spells);
        return mechanics;
    }

    private static Spell spellWithUrl(String url) {
        Spell spell = new Spell();
        spell.setUrl(url);
        return spell;
    }

    private static SpellShortResponse shortOf(String name) {
        SpellShortResponse response = new SpellShortResponse();
        response.setUrl(name);
        return response;
    }
}
