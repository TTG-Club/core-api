package club.ttg.dnd5.domain.persistence;

import club.ttg.dnd5.PortalApplication;
import club.ttg.dnd5.domain.background.repository.BackgroundRepository;
import club.ttg.dnd5.domain.background.rest.dto.BackgroundRequest;
import club.ttg.dnd5.domain.background.service.BackgroundService;
import club.ttg.dnd5.domain.character_class.model.CharacterClass;
import club.ttg.dnd5.domain.character_class.repository.ClassRepository;
import club.ttg.dnd5.domain.common.rest.dto.NameRequest;
import club.ttg.dnd5.domain.common.rest.dto.SourceRequest;
import club.ttg.dnd5.domain.feat.model.Feat;
import club.ttg.dnd5.domain.feat.model.FeatCategory;
import club.ttg.dnd5.domain.feat.repository.FeatRepository;
import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.source.model.SourceType;
import club.ttg.dnd5.domain.source.repository.SourceRepository;
import club.ttg.dnd5.domain.species.model.Species;
import club.ttg.dnd5.domain.species.repository.SpeciesRepository;
import club.ttg.dnd5.domain.spell.model.Spell;
import club.ttg.dnd5.domain.spell.model.SpellCastingTime;
import club.ttg.dnd5.domain.spell.model.SpellComponents;
import club.ttg.dnd5.domain.spell.model.SpellDistance;
import club.ttg.dnd5.domain.spell.model.SpellDuration;
import club.ttg.dnd5.domain.spell.model.enums.CastingUnit;
import club.ttg.dnd5.domain.spell.model.enums.DistanceUnit;
import club.ttg.dnd5.domain.spell.model.enums.DurationUnit;
import club.ttg.dnd5.domain.spell.model.enums.MagicSchool;
import club.ttg.dnd5.domain.spell.repository.SpellRepository;
import club.ttg.dnd5.domain.spell.rest.dto.create.CreateAffiliationRequest;
import club.ttg.dnd5.domain.spell.rest.dto.create.SpellRequest;
import club.ttg.dnd5.domain.spell.service.SpellService;
import club.ttg.dnd5.domain.user.model.Role;
import club.ttg.dnd5.domain.user.model.User;
import club.ttg.dnd5.domain.user.repository.RoleRepository;
import club.ttg.dnd5.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(
        classes = PortalApplication.class,
        properties = {
                "app.url=http://localhost",
                "api.secret=test-secret",
                "spring.liquibase.enabled=false",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.jpa.properties.hibernate.hbm2ddl.auto=create-drop",
                "spring.jpa.properties.hibernate.generate_statistics=true",
                "spring.jpa.properties.hibernate.boot.allow_jdbc_metadata_access=true",
                "spring.mail.username=test@example.com",
                "spring.mail.password=test-password",
                "spring.cloud.aws.s3.endpoint=http://localhost:4566",
                "spring.cloud.aws.s3.region=us-east-1",
                "spring.cloud.aws.s3.bucket=test-bucket",
                "spring.cloud.aws.credentials.access-key=test",
                "spring.cloud.aws.credentials.secret-key=test"
        }
)
@Transactional
class RelatedEntitySaveIsolationIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withInitScript("db/test/init-related-entity-save-isolation.sql");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @MockitoBean
    S3Client s3Client;

    @Autowired
    SpellService spellService;
    @Autowired
    BackgroundService backgroundService;
    @Autowired
    SpellRepository spellRepository;
    @Autowired
    BackgroundRepository backgroundRepository;
    @Autowired
    SourceRepository sourceRepository;
    @Autowired
    ClassRepository classRepository;
    @Autowired
    SpeciesRepository speciesRepository;
    @Autowired
    FeatRepository featRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    EntityManager entityManager;
    @Autowired
    EntityManagerFactory entityManagerFactory;

    private Statistics statistics;

    @BeforeEach
    void setUp() {
        statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.setStatisticsEnabled(true);

        backgroundRepository.deleteAll();
        spellRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
        featRepository.deleteAll();
        classRepository.deleteAll();
        speciesRepository.deleteAll();
        sourceRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
        statistics.clear();
    }

    @Test
    void savingSpellDoesNotUpdateAffiliatedEntities() {
        Fixture fixture = persistFixture("spell-save");
        statistics.clear();

        spellService.save(spellRequest("spell-save", fixture));
        entityManager.flush();

        assertNoUpdatesForRelatedEntities();
        assertThat(spellRepository.findById("spell-save")).isPresent();
    }

    @Test
    void updatingSpellDoesNotUpdateAffiliatedEntities() {
        Fixture fixture = persistFixture("spell-update");
        spellService.save(spellRequest("spell-update", fixture));
        entityManager.flush();
        entityManager.clear();
        statistics.clear();

        SpellRequest request = spellRequest("spell-update", fixture);
        request.setDescription("Changed spell description");

        spellService.update("spell-update", request);
        entityManager.flush();

        assertNoUpdatesForRelatedEntities();
        assertThat(statistics.getEntityStatistics(Spell.class.getName()).getUpdateCount()).isEqualTo(1);
    }

    @Test
    void savingBackgroundDoesNotUpdateFeatOrSource() {
        Source source = saveSource("background-source");
        Feat feat = saveFeat("background-feat", source);
        entityManager.flush();
        entityManager.clear();
        statistics.clear();

        backgroundService.addBackground(backgroundRequest(source, feat));
        entityManager.flush();

        assertThat(statistics.getEntityStatistics(Source.class.getName()).getUpdateCount()).isZero();
        assertThat(statistics.getEntityStatistics(Feat.class.getName()).getUpdateCount()).isZero();
    }

    @Test
    void savingUserDoesNotUpdateExistingRoles() {
        Role role = roleRepository.saveAndFlush(Role.builder().name("ROLE_TEST").build());
        entityManager.clear();
        statistics.clear();

        User user = new User();
        user.setUsername("role-user");
        user.setEmail("role-user@example.com");
        user.setPassword("password");
        user.setEnabled(true);
        user.setRoles(List.of(role));

        userRepository.saveAndFlush(user);

        assertThat(statistics.getEntityStatistics(Role.class.getName()).getUpdateCount()).isZero();
    }

    private void assertNoUpdatesForRelatedEntities() {
        assertThat(statistics.getEntityStatistics(Source.class.getName()).getUpdateCount()).isZero();
        assertThat(statistics.getEntityStatistics(CharacterClass.class.getName()).getUpdateCount()).isZero();
        assertThat(statistics.getEntityStatistics(Species.class.getName()).getUpdateCount()).isZero();
        assertThat(statistics.getEntityStatistics(Feat.class.getName()).getUpdateCount()).isZero();
    }

    private Fixture persistFixture(String prefix) {
        Source source = saveSource(prefix + "-source");
        CharacterClass characterClass = saveClass(prefix + "-class", source);
        CharacterClass subclass = saveClass(prefix + "-subclass", source);
        Species species = saveSpecies(prefix + "-species", source);
        Species lineage = saveSpecies(prefix + "-lineage", source);
        Feat feat = saveFeat(prefix + "-feat", source);
        entityManager.flush();
        entityManager.clear();
        return new Fixture(source, characterClass, subclass, species, lineage, feat);
    }

    private Source saveSource(String id) {
        Source source = new Source();
        source.setAcronym(id.toUpperCase().replace('-', '_'));
        source.setUrl(id);
        source.setName(id + " name");
        source.setEnglish(id + " english");
        source.setType(SourceType.OFFICIAL);
        return sourceRepository.saveAndFlush(source);
    }

    private CharacterClass saveClass(String url, Source source) {
        CharacterClass characterClass = new CharacterClass();
        characterClass.setUrl(url);
        characterClass.setName(url + " name");
        characterClass.setEnglish(url + " english");
        characterClass.setSource(source);
        return classRepository.saveAndFlush(characterClass);
    }

    private Species saveSpecies(String url, Source source) {
        Species species = new Species();
        species.setUrl(url);
        species.setName(url + " name");
        species.setEnglish(url + " english");
        species.setSource(source);
        return speciesRepository.saveAndFlush(species);
    }

    private Feat saveFeat(String url, Source source) {
        Feat feat = new Feat();
        feat.setUrl(url);
        feat.setName(url + " name");
        feat.setEnglish(url + " english");
        feat.setCategory(FeatCategory.GENERAL);
        feat.setSource(source);
        return featRepository.saveAndFlush(feat);
    }

    private SpellRequest spellRequest(String url, Fixture fixture) {
        SpellRequest request = new SpellRequest();
        request.setUrl(url);
        request.setName(name(url));
        request.setDescription(url + " description");
        request.setSource(sourceRequest(fixture.source()));
        request.setLevel(1L);
        request.setSchool(MagicSchool.ABJURATION);
        request.setComponents(SpellComponents.builder().v(true).s(true).build());
        request.setCastingTime(List.of(SpellCastingTime.of(null, CastingUnit.ACTION)));
        request.setRange(List.of(SpellDistance.of(30L, DistanceUnit.FEET)));
        request.setDuration(List.of(SpellDuration.of(1L, DurationUnit.MINUTE)));
        request.setAffiliations(CreateAffiliationRequest.builder()
                .classes(Set.of(fixture.characterClass().getUrl()))
                .subclasses(Set.of(fixture.subclass().getUrl()))
                .species(Set.of(fixture.species().getUrl()))
                .lineages(Set.of(fixture.lineage().getUrl()))
                .feats(Set.of(fixture.feat().getUrl()))
                .build());
        return request;
    }

    private BackgroundRequest backgroundRequest(Source source, Feat feat) {
        BackgroundRequest request = new BackgroundRequest();
        request.setUrl("background");
        request.setName(name("background"));
        request.setDescription("background" + " description");
        request.setSource(sourceRequest(source));
        request.setFeatUrl(feat.getUrl());
        return request;
    }

    private SourceRequest sourceRequest(Source source) {
        SourceRequest request = new SourceRequest();
        request.setUrl(source.getUrl());
        request.setPage(1);
        return request;
    }

    private NameRequest name(String value) {
        NameRequest name = new NameRequest();
        name.setName(value + " name");
        name.setEnglish(value + " english");
        name.setAlternative(List.of());
        return name;
    }

    private record Fixture(
            Source source,
            CharacterClass characterClass,
            CharacterClass subclass,
            Species species,
            Species lineage,
            Feat feat
    ) {
    }
}
