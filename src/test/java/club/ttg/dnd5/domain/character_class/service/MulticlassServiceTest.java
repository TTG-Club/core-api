package club.ttg.dnd5.domain.character_class.service;

import club.ttg.dnd5.domain.character_class.model.CasterType;
import club.ttg.dnd5.domain.character_class.model.CharacterClass;
import club.ttg.dnd5.domain.character_class.model.ClassFeature;
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
}
