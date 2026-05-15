package club.ttg.dnd5.domain.character_class.service;

import club.ttg.dnd5.domain.character_class.model.CasterType;
import club.ttg.dnd5.domain.character_class.model.CharacterClass;
import club.ttg.dnd5.domain.character_class.model.ClassFeature;
import club.ttg.dnd5.domain.character_class.model.ClassTableColumn;
import club.ttg.dnd5.domain.character_class.model.ClassTableItem;
import club.ttg.dnd5.domain.character_class.model.MulticlassProficiency;
import club.ttg.dnd5.domain.character_class.model.WeaponProficiency;
import club.ttg.dnd5.domain.character_class.repository.ClassRepository;
import club.ttg.dnd5.domain.character_class.rest.dto.ClassFeatureDto;
import club.ttg.dnd5.domain.character_class.rest.dto.MulticlassResponse;
import club.ttg.dnd5.domain.character_class.rest.mapper.ClassFeatureMapper;
import club.ttg.dnd5.domain.character_class.rest.mapper.MulticlassMapper;
import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.Dice;
import club.ttg.dnd5.domain.common.dictionary.WeaponCategory;
import club.ttg.dnd5.domain.common.rest.dto.MulticlassDto;
import club.ttg.dnd5.domain.common.rest.dto.MulticlassLevelEntry;
import club.ttg.dnd5.domain.common.rest.dto.MulticlassRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MulticlassServiceTest {
    private final ClassRepository classRepository = mock(ClassRepository.class);
    private final MulticlassMapper multiclassMapper = mock(MulticlassMapper.class);
    private final ClassFeatureMapper classFeatureMapper = mock(ClassFeatureMapper.class);
    private final MulticlassService service = new MulticlassService(
            classRepository,
            multiclassMapper,
            classFeatureMapper
    );

    @Test
    void getMulticlassMergesWeaponProficiencyWhenStartingClassHasNone() {
        CharacterClass mainClass = characterClass("main");
        mainClass.setWeaponProficiency(null);

        CharacterClass addedClass = characterClass("added");
        MulticlassProficiency multiclassProficiency = new MulticlassProficiency();
        multiclassProficiency.setWeapon(new WeaponProficiency(Set.of(WeaponCategory.SIMPLE_MELEE), null));
        addedClass.setMulticlassProficiency(multiclassProficiency);

        MulticlassRequest request = new MulticlassRequest();
        request.setUrl("main");
        request.setLevel(1);
        MulticlassDto addedClassRequest = new MulticlassDto();
        addedClassRequest.setUrl("added");
        addedClassRequest.setLevel(1);
        request.setClasses(List.of(addedClassRequest));

        when(classRepository.findById("main")).thenReturn(Optional.of(mainClass));
        when(classRepository.findById("added")).thenReturn(Optional.of(addedClass));
        when(multiclassMapper.toMulticlassResponse(any(CharacterClass.class))).thenReturn(new MulticlassResponse());

        service.getMulticlass(request);

        ArgumentCaptor<CharacterClass> captor = ArgumentCaptor.forClass(CharacterClass.class);
        verify(multiclassMapper).toMulticlassResponse(captor.capture());
        assertTrue(captor.getValue().getWeaponProficiency().getCategory().contains(WeaponCategory.SIMPLE_MELEE));
    }

    @Test
    void getMulticlassUsesMulticlassCasterTypeWhenAnyClassOrSubclassIsCaster() {
        CharacterClass fighter = characterClass("fighter");
        CharacterClass eldritchKnight = characterClass("eldritch-knight");
        eldritchKnight.setCasterType(CasterType.THIRD);

        CharacterClass rogue = characterClass("rogue");
        CharacterClass arcaneTrickster = characterClass("arcane-trickster");
        arcaneTrickster.setCasterType(CasterType.THIRD);

        MulticlassRequest request = new MulticlassRequest();
        request.setUrl("fighter");
        request.setSubclass("eldritch-knight");
        request.setLevel(3);
        MulticlassDto rogueRequest = new MulticlassDto();
        rogueRequest.setUrl("rogue");
        rogueRequest.setSubclass("arcane-trickster");
        rogueRequest.setLevel(3);
        request.setClasses(List.of(rogueRequest));

        MulticlassResponse response = new MulticlassResponse();
        when(classRepository.findById("fighter")).thenReturn(Optional.of(fighter));
        when(classRepository.findById("eldritch-knight")).thenReturn(Optional.of(eldritchKnight));
        when(classRepository.findById("rogue")).thenReturn(Optional.of(rogue));
        when(classRepository.findById("arcane-trickster")).thenReturn(Optional.of(arcaneTrickster));
        when(multiclassMapper.toMulticlassResponse(any(CharacterClass.class))).thenReturn(response);

        service.getMulticlass(request);

        ArgumentCaptor<CharacterClass> captor = ArgumentCaptor.forClass(CharacterClass.class);
        verify(multiclassMapper).toMulticlassResponse(captor.capture());
        assertEquals(CasterType.MULTICLASS, captor.getValue().getCasterType());
        assertEquals(2, response.getSpellcastingLevel());
    }

    @Test
    void getMulticlassDoesNotDuplicateSpellcastingFeatureFromAdditionalSubclass() {
        CharacterClass fighter = characterClass("fighter");
        CharacterClass eldritchKnight = characterClass("eldritch-knight");
        eldritchKnight.setCasterType(CasterType.THIRD);
        eldritchKnight.setFeatures(List.of(feature()));

        CharacterClass rogue = characterClass("rogue");
        CharacterClass arcaneTrickster = characterClass("arcane-trickster");
        arcaneTrickster.setCasterType(CasterType.THIRD);
        arcaneTrickster.setFeatures(List.of(feature()));

        MulticlassRequest request = new MulticlassRequest();
        request.setUrl("fighter");
        request.setSubclass("eldritch-knight");
        request.setLevel(3);
        MulticlassDto rogueRequest = new MulticlassDto();
        rogueRequest.setUrl("rogue");
        rogueRequest.setSubclass("arcane-trickster");
        rogueRequest.setLevel(3);
        request.setClasses(List.of(rogueRequest));

        MulticlassResponse response = new MulticlassResponse();
        when(classRepository.findById("fighter")).thenReturn(Optional.of(fighter));
        when(classRepository.findById("eldritch-knight")).thenReturn(Optional.of(eldritchKnight));
        when(classRepository.findById("rogue")).thenReturn(Optional.of(rogue));
        when(classRepository.findById("arcane-trickster")).thenReturn(Optional.of(arcaneTrickster));
        when(classFeatureMapper.toDto(any(ClassFeature.class), anyBoolean()))
                .thenAnswer(invocation -> new ClassFeatureDto(invocation.getArgument(0), invocation.getArgument(1)));
        when(multiclassMapper.toMulticlassResponse(any(CharacterClass.class))).thenReturn(response);

        service.getMulticlass(request);

        long spellcastingFeatures = response.getFeatures()
                .stream()
                .filter(feature -> feature.getName().equals("Использование заклинаний"))
                .count();
        assertEquals(1, spellcastingFeatures);
    }

    @Test
    void getMulticlassWithLevelsFormatSupportsRepeatedClassEntries() {
        // Сценарий: Воин 3, Волшебник 2, Воин 4 (ещё 1 уровень воина)
        // Умения должны идти в порядке взятия уровней
        CharacterClass fighter = characterClass("fighter");
        ClassFeature fighterFeature1 = classFeature("Боевой стиль", 1);
        ClassFeature fighterFeature2 = classFeature("Всплеск действий", 2);
        ClassFeature fighterFeature3 = classFeature("Архетип воина", 3);
        ClassFeature fighterFeature4 = classFeature("Увеличение характеристик", 4);
        fighter.setFeatures(List.of(fighterFeature1, fighterFeature2, fighterFeature3, fighterFeature4));

        CharacterClass wizard = characterClass("wizard");
        wizard.setCasterType(CasterType.FULL);
        ClassFeature wizardFeature1 = classFeature("Использование заклинаний", 1);
        ClassFeature wizardFeature2 = classFeature("Магическое восстановление", 1);
        ClassFeature wizardFeature3 = classFeature("Магическая традиция", 2);
        wizard.setFeatures(List.of(wizardFeature1, wizardFeature2, wizardFeature3));

        MulticlassRequest request = new MulticlassRequest();
        // level = абсолютный уровень класса после этого сегмента
        request.setLevels(List.of(
                new MulticlassLevelEntry("fighter", null, 3),  // воин 1-3
                new MulticlassLevelEntry("wizard", null, 2),   // волшебник 1-2
                new MulticlassLevelEntry("fighter", null, 4)   // воин 4 (ещё 1 уровень)
        ));

        MulticlassResponse response = new MulticlassResponse();
        when(classRepository.findById("fighter")).thenReturn(Optional.of(fighter));
        when(classRepository.findById("wizard")).thenReturn(Optional.of(wizard));
        when(classFeatureMapper.toDto(any(ClassFeature.class), anyBoolean()))
                .thenAnswer(invocation -> new ClassFeatureDto(invocation.getArgument(0), invocation.getArgument(1)));
        when(multiclassMapper.toMulticlassResponse(any(CharacterClass.class))).thenReturn(response);

        service.getMulticlass(request);

        // Total character level should be 6
        assertEquals(6, response.getCharacterLevel());

        // Features should be ordered by character level:
        // Fighter features at char levels 1,2,3 (class levels 1,2,3)
        // Wizard features at char levels 4,4,5 (class levels 1,1,2)
        // Fighter feature at char level 6 (class level 4)
        List<ClassFeatureDto> features = response.getFeatures();
        assertEquals(7, features.size());

        // First 3 features are from fighter (levels 1-3)
        assertEquals("Боевой стиль", features.get(0).getName());
        assertEquals(1, features.get(0).getLevel());
        assertEquals("Всплеск действий", features.get(1).getName());
        assertEquals(2, features.get(1).getLevel());
        assertEquals("Архетип воина", features.get(2).getName());
        assertEquals(3, features.get(2).getLevel());

        // Next 3 features are from wizard (char levels 4-5)
        assertEquals("Использование заклинаний", features.get(3).getName());
        assertEquals(4, features.get(3).getLevel());
        assertEquals("Магическое восстановление", features.get(4).getName());
        assertEquals(4, features.get(4).getLevel());
        assertEquals("Магическая традиция", features.get(5).getName());
        assertEquals(5, features.get(5).getLevel());

        // Last feature is fighter level 4 at char level 6
        assertEquals("Увеличение характеристик", features.get(6).getName());
        assertEquals(6, features.get(6).getLevel());
    }

    @Test
    void getMulticlassWithLevelsFormatReturnsFeaturesForEachRepeatedClassSegment() {
        CharacterClass fighter = characterClass("fighter-phb");
        fighter.setFeatures(List.of(
                classFeature("fighter-1", 1),
                classFeature("fighter-2", 2),
                classFeature("fighter-3", 3),
                classFeature("fighter-4", 4),
                classFeature("fighter-5", 5),
                classFeature("fighter-6", 6)
        ));

        CharacterClass bard = characterClass("bard-phb");
        bard.setFeatures(List.of(
                classFeature("bard-1", 1),
                classFeature("bard-2", 2),
                classFeature("bard-3", 3)
        ));

        MulticlassRequest request = new MulticlassRequest();
        request.setLevels(List.of(
                new MulticlassLevelEntry("fighter-phb", "battle-master-phb", 3),
                new MulticlassLevelEntry("bard-phb", "bard-college-of-valor-phb", 3),
                new MulticlassLevelEntry("fighter-phb", "battle-master-phb", 6)
        ));

        MulticlassResponse response = new MulticlassResponse();
        when(classRepository.findById("fighter-phb")).thenReturn(Optional.of(fighter));
        when(classRepository.findById("bard-phb")).thenReturn(Optional.of(bard));
        when(classRepository.findById("battle-master-phb")).thenReturn(Optional.of(characterClass("battle-master-phb")));
        when(classRepository.findById("bard-college-of-valor-phb"))
                .thenReturn(Optional.of(characterClass("bard-college-of-valor-phb")));
        when(classFeatureMapper.toDto(any(ClassFeature.class), anyBoolean()))
                .thenAnswer(invocation -> new ClassFeatureDto(invocation.getArgument(0), invocation.getArgument(1)));
        when(multiclassMapper.toMulticlassResponse(any(CharacterClass.class))).thenReturn(response);

        service.getMulticlass(request);

        List<String> featureNames = response.getFeatures()
                .stream()
                .map(ClassFeatureDto::getName)
                .toList();
        assertEquals(List.of(
                "fighter-1",
                "fighter-2",
                "fighter-3",
                "bard-1",
                "bard-2",
                "bard-3",
                "fighter-4",
                "fighter-5",
                "fighter-6"
        ), featureNames);
        assertEquals(9, response.getCharacterLevel());
    }

    @Test
    void getMulticlassWithLevelsFormatCalculatesSpellcastingLevelCorrectly() {
        // Волшебник 3, Воин 2, Волшебник 5 = итого 5 уровней волшебника, уровень заклинателя = 5
        CharacterClass wizard = characterClass("wizard");
        wizard.setCasterType(CasterType.FULL);

        CharacterClass fighter = characterClass("fighter");

        MulticlassRequest request = new MulticlassRequest();
        // level = абсолютный уровень класса после этого сегмента
        request.setLevels(List.of(
                new MulticlassLevelEntry("wizard", null, 3),  // волшебник 1-3
                new MulticlassLevelEntry("fighter", null, 2), // воин 1-2
                new MulticlassLevelEntry("wizard", null, 5)   // волшебник 4-5
        ));

        MulticlassResponse response = new MulticlassResponse();
        when(classRepository.findById("wizard")).thenReturn(Optional.of(wizard));
        when(classRepository.findById("fighter")).thenReturn(Optional.of(fighter));
        when(multiclassMapper.toMulticlassResponse(any(CharacterClass.class))).thenReturn(response);

        service.getMulticlass(request);

        assertEquals(7, response.getCharacterLevel());
        assertEquals(5, response.getSpellcastingLevel());
    }

    @Test
    void getMulticlassWithLevelsFormatMergesProficiencyOnlyOnce() {
        // Воин 3, Волшебник 2, Воин 4 — владения волшебника должны добавиться только один раз
        CharacterClass fighter = characterClass("fighter");
        CharacterClass wizard = characterClass("wizard");
        MulticlassProficiency wizardMulticlass = new MulticlassProficiency();
        wizardMulticlass.setWeapon(new WeaponProficiency(Set.of(WeaponCategory.SIMPLE_MELEE), null));
        wizard.setMulticlassProficiency(wizardMulticlass);

        MulticlassRequest request = new MulticlassRequest();
        // level = абсолютный уровень класса после этого сегмента
        request.setLevels(List.of(
                new MulticlassLevelEntry("fighter", null, 3),  // воин 1-3
                new MulticlassLevelEntry("wizard", null, 2),   // волшебник 1-2
                new MulticlassLevelEntry("fighter", null, 4)   // воин 4 (ещё 1 уровень)
        ));

        MulticlassResponse response = new MulticlassResponse();
        when(classRepository.findById("fighter")).thenReturn(Optional.of(fighter));
        when(classRepository.findById("wizard")).thenReturn(Optional.of(wizard));
        when(multiclassMapper.toMulticlassResponse(any(CharacterClass.class))).thenReturn(response);

        service.getMulticlass(request);

        ArgumentCaptor<CharacterClass> captor = ArgumentCaptor.forClass(CharacterClass.class);
        verify(multiclassMapper).toMulticlassResponse(captor.capture());
        // Weapon proficiency should contain SIMPLE_MELEE from wizard multiclass proficiency
        assertTrue(captor.getValue().getWeaponProficiency().getCategory().contains(WeaponCategory.SIMPLE_MELEE));
    }

    @Test
    void getMulticlassWithLevelsFormatMergesTableColumnsForRepeatedClass() {
        // Fighter 3 → Wizard 2 → Fighter 4: Fighter's table columns must appear once, not twice
        CharacterClass fighter = characterClass("fighter");
        ClassTableColumn fighterCol = new ClassTableColumn();
        fighterCol.setName("Всплески действий");
        ClassTableItem item1 = new ClassTableItem(); item1.setLevel(2); item1.setValue("1");
        ClassTableItem item2 = new ClassTableItem(); item2.setLevel(3); item2.setValue("1");
        ClassTableItem item3 = new ClassTableItem(); item3.setLevel(4); item3.setValue("2");
        fighterCol.setScaling(new java.util.ArrayList<>(List.of(item1, item2, item3)));
        fighter.setTable(List.of(fighterCol));

        CharacterClass wizard = characterClass("wizard");
        wizard.setTable(List.of());

        MulticlassRequest request = new MulticlassRequest();
        request.setLevels(List.of(
                new MulticlassLevelEntry("fighter", null, 3),
                new MulticlassLevelEntry("wizard", null, 2),
                new MulticlassLevelEntry("fighter", null, 4)
        ));

        MulticlassResponse response = new MulticlassResponse();
        when(classRepository.findById("fighter")).thenReturn(Optional.of(fighter));
        when(classRepository.findById("wizard")).thenReturn(Optional.of(wizard));
        when(multiclassMapper.toMulticlassResponse(any(CharacterClass.class))).thenReturn(response);

        service.getMulticlass(request);

        ArgumentCaptor<CharacterClass> captor = ArgumentCaptor.forClass(CharacterClass.class);
        verify(multiclassMapper).toMulticlassResponse(captor.capture());

        List<ClassTableColumn> table = captor.getValue().getTable();
        // There must be exactly one column named "Всплески действий"
        long count = table.stream().filter(c -> c.getName().equals("Всплески действий")).count();
        assertEquals(1, count, "Duplicate table columns found for repeated class");

        // That single column must contain all 3 scaling rows (levels 2,3 from first segment + level 4 from second)
        ClassTableColumn merged = table.stream()
                .filter(c -> c.getName().equals("Всплески действий"))
                .findFirst()
                .orElseThrow();
        assertEquals(3, merged.getScaling().size());
    }

    private CharacterClass characterClass(String url) {
        CharacterClass characterClass = new CharacterClass();
        characterClass.setUrl(url);
        characterClass.setName(url);
        characterClass.setHitDice(Dice.d8);
        characterClass.setCasterType(CasterType.NONE);
        characterClass.setPrimaryCharacteristics(Set.of(Ability.STRENGTH));
        characterClass.setSavingThrows(Set.of(Ability.STRENGTH));
        characterClass.setFeatures(List.of());
        characterClass.setTable(List.of());
        return characterClass;
    }

    private ClassFeature feature() {
        ClassFeature classFeature = new ClassFeature();
        classFeature.setName("Использование заклинаний");
        classFeature.setLevel(3);
        classFeature.setScaling(List.of());
        classFeature.setOptions(List.of());
        return classFeature;
    }

    private ClassFeature classFeature(String name, int level) {
        ClassFeature classFeature = new ClassFeature();
        classFeature.setName(name);
        classFeature.setLevel(level);
        classFeature.setScaling(List.of());
        classFeature.setOptions(List.of());
        return classFeature;
    }
}
