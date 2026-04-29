package club.ttg.dnd5.domain.character_class.service;

import club.ttg.dnd5.domain.character_class.model.CasterType;
import club.ttg.dnd5.domain.character_class.model.CharacterClass;
import club.ttg.dnd5.domain.character_class.model.MulticlassProficiency;
import club.ttg.dnd5.domain.character_class.model.WeaponProficiency;
import club.ttg.dnd5.domain.character_class.repository.ClassRepository;
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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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
}
