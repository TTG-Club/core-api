package club.ttg.dnd5.domain.background.service;

import club.ttg.dnd5.domain.background.model.BackgroundToolChoice;
import club.ttg.dnd5.domain.background.rest.dto.BackgroundDetailResponse;
import club.ttg.dnd5.domain.common.model.EntityRef;
import club.ttg.dnd5.domain.feat.repository.FeatNameRef;
import club.ttg.dnd5.domain.feat.repository.FeatRepository;
import club.ttg.dnd5.domain.item.repository.ItemNameRef;
import club.ttg.dnd5.domain.item.repository.ItemRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class BackgroundRefNameResolverTest {
    private final ItemRepository itemRepository = mock(ItemRepository.class);
    private final FeatRepository featRepository = mock(FeatRepository.class);
    private final BackgroundRefNameResolver resolver = new BackgroundRefNameResolver(itemRepository, featRepository);

    /** Ссылка без снимка названия получает его из справочника — так «Послушник» отдаёт инструмент. */
    @Test
    void namesToolWithoutSnapshot() {
        var response = new BackgroundDetailResponse();
        response.setToolProficiencies(List.of(new EntityRef("calligrapher-s-supplies-phb", null)));

        when(itemRepository.findNamesByUrls(Set.of("calligrapher-s-supplies-phb")))
                .thenReturn(List.of(itemName("calligrapher-s-supplies-phb", "Инструменты каллиграфа")));

        resolver.resolveNames(response);

        assertThat(response.getToolProficiencies())
                .extracting(EntityRef::getUrl, EntityRef::getName)
                .containsExactly(tuple("calligrapher-s-supplies-phb", "Инструменты каллиграфа"));
    }

    /** Пул выбора инструмента и фиксированные инструменты — один запрос к предметам. */
    @Test
    void namesToolChoicePoolWithSameLookup() {
        var response = new BackgroundDetailResponse();
        response.setToolProficiencies(List.of(new EntityRef("thieves-tools-phb", null)));
        response.setToolChoice(new BackgroundToolChoice(1, List.of(new EntityRef("dice-set-phb", null))));

        when(itemRepository.findNamesByUrls(Set.of("thieves-tools-phb", "dice-set-phb")))
                .thenReturn(List.of(
                        itemName("thieves-tools-phb", "Воровские инструменты"),
                        itemName("dice-set-phb", "Игральные кости")));

        resolver.resolveNames(response);

        assertThat(response.getToolChoice().getCount()).isEqualTo(1);
        assertThat(response.getToolChoice().getFrom())
                .extracting(EntityRef::getName)
                .containsExactly("Игральные кости");
    }

    /** Черты на выбор называются по справочнику черт. */
    @Test
    void namesFeatChoices() {
        var response = new BackgroundDetailResponse();
        response.setFeatChoices(List.of(new EntityRef("gathered-whispers-rhw", null)));

        when(featRepository.findNamesByUrls(Set.of("gathered-whispers-rhw")))
                .thenReturn(List.of(featName("gathered-whispers-rhw", "Собранный шёпот")));

        resolver.resolveNames(response);

        assertThat(response.getFeatChoices())
                .extracting(EntityRef::getName)
                .containsExactly("Собранный шёпот");
        verifyNoInteractions(itemRepository);
    }

    /** Записи уже нет в справочнике — остаётся снимок из ссылки. */
    @Test
    void keepsSnapshotWhenRecordMissing() {
        var snapshot = new EntityRef("lost-tool", "Снимок");
        var response = new BackgroundDetailResponse();
        response.setToolProficiencies(List.of(snapshot));

        when(itemRepository.findNamesByUrls(Set.of("lost-tool"))).thenReturn(List.of());

        resolver.resolveNames(response);

        assertThat(response.getToolProficiencies())
                .extracting(EntityRef::getName)
                .containsExactly("Снимок");
    }

    /** Ссылки пересоздаются: объект из jsonb-поля сущности не меняется. */
    @Test
    void doesNotMutateOriginalRefs() {
        var original = new EntityRef("calligrapher-s-supplies-phb", null);
        var response = new BackgroundDetailResponse();
        response.setToolProficiencies(List.of(original));

        when(itemRepository.findNamesByUrls(Set.of("calligrapher-s-supplies-phb")))
                .thenReturn(List.of(itemName("calligrapher-s-supplies-phb", "Инструменты каллиграфа")));

        resolver.resolveNames(response);

        assertThat(original.getName()).isNull();
    }

    /** Исходный выбор инструмента не меняется: в ответ кладётся новый объект. */
    @Test
    void doesNotMutateOriginalToolChoice() {
        var originalRef = new EntityRef("dice-set-phb", null);
        var originalChoice = new BackgroundToolChoice(1, List.of(originalRef));
        var response = new BackgroundDetailResponse();
        response.setToolChoice(originalChoice);

        when(itemRepository.findNamesByUrls(Set.of("dice-set-phb")))
                .thenReturn(List.of(itemName("dice-set-phb", "Игральные кости")));

        resolver.resolveNames(response);

        assertThat(response.getToolChoice()).isNotSameAs(originalChoice);
        assertThat(originalChoice.getFrom()).containsExactly(originalRef);
        assertThat(originalRef.getName()).isNull();
    }

    /** Пустой пул значит «любой инструмент» и остаётся пустым, а не превращается в null. */
    @Test
    void keepsEmptyToolChoicePool() {
        var response = new BackgroundDetailResponse();
        response.setToolChoice(new BackgroundToolChoice(1, List.of()));

        resolver.resolveNames(response);

        assertThat(response.getToolChoice().getCount()).isEqualTo(1);
        assertThat(response.getToolChoice().getFrom()).isEmpty();
        verifyNoInteractions(itemRepository);
    }

    /** Выбор без пула остаётся без пула. */
    @Test
    void keepsNullToolChoicePool() {
        var response = new BackgroundDetailResponse();
        response.setToolChoice(new BackgroundToolChoice(2, null));

        resolver.resolveNames(response);

        assertThat(response.getToolChoice().getCount()).isEqualTo(2);
        assertThat(response.getToolChoice().getFrom()).isNull();
    }

    /** Без выбора инструмента ответ так и остаётся без него. */
    @Test
    void keepsAbsentToolChoice() {
        var response = new BackgroundDetailResponse();
        response.setToolProficiencies(List.of(new EntityRef("thieves-tools-phb", null)));

        when(itemRepository.findNamesByUrls(Set.of("thieves-tools-phb"))).thenReturn(List.of());

        resolver.resolveNames(response);

        assertThat(response.getToolChoice()).isNull();
    }

    /** Повтор url не роняет подстановку, пустые элементы выпадают. */
    @Test
    void handlesDuplicateUrlsAndNullRefs() {
        var response = new BackgroundDetailResponse();
        var refs = new ArrayList<EntityRef>();
        refs.add(new EntityRef("thieves-tools-phb", null));
        refs.add(null);
        refs.add(new EntityRef("thieves-tools-phb", null));
        response.setToolProficiencies(refs);

        when(itemRepository.findNamesByUrls(Set.of("thieves-tools-phb")))
                .thenReturn(List.of(itemName("thieves-tools-phb", "Воровские инструменты")));

        resolver.resolveNames(response);

        assertThat(response.getToolProficiencies())
                .extracting(EntityRef::getName)
                .containsExactly("Воровские инструменты", "Воровские инструменты");
    }

    /** Инструменты и черты одной предыстории — по одному запросу в каждый справочник. */
    @Test
    void resolvesToolsAndFeatsTogether() {
        var response = new BackgroundDetailResponse();
        response.setToolProficiencies(List.of(new EntityRef("disguise-kit-phb", null)));
        response.setFeatChoices(List.of(new EntityRef("sharp-eye-rhw", null)));

        when(itemRepository.findNamesByUrls(Set.of("disguise-kit-phb")))
                .thenReturn(List.of(itemName("disguise-kit-phb", "Набор для маскировки")));
        when(featRepository.findNamesByUrls(Set.of("sharp-eye-rhw")))
                .thenReturn(List.of(featName("sharp-eye-rhw", "Острый глаз")));

        resolver.resolveNames(response);

        assertThat(response.getToolProficiencies()).extracting(EntityRef::getName)
                .containsExactly("Набор для маскировки");
        assertThat(response.getFeatChoices()).extracting(EntityRef::getName)
                .containsExactly("Острый глаз");
    }

    /** Предыстория без ссылок не ходит в справочники. */
    @Test
    void emptyBackgroundDoesNotHitRepositories() {
        resolver.resolveNames(new BackgroundDetailResponse());

        verifyNoInteractions(itemRepository);
        verifyNoInteractions(featRepository);
    }

    private ItemNameRef itemName(String url, String name) {
        return new ItemNameRef() {
            @Override
            public String getUrl() {
                return url;
            }

            @Override
            public String getName() {
                return name;
            }
        };
    }

    private FeatNameRef featName(String url, String name) {
        return new FeatNameRef() {
            @Override
            public String getUrl() {
                return url;
            }

            @Override
            public String getName() {
                return name;
            }
        };
    }
}
