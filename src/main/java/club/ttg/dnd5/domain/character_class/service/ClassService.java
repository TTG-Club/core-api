package club.ttg.dnd5.domain.character_class.service;

import club.ttg.dnd5.domain.character_class.rest.dto.ClassAbilityImprovementResponse;
import club.ttg.dnd5.domain.character_class.rest.dto.ClassProficiencyDto;
import club.ttg.dnd5.domain.character_class.rest.dto.ClassQueryRequest;
import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.source.service.SourceSavedFilterService;
import club.ttg.dnd5.domain.source.service.SourceService;
import club.ttg.dnd5.domain.character_class.model.CasterType;
import club.ttg.dnd5.domain.character_class.model.CharacterClass;
import club.ttg.dnd5.domain.character_class.repository.ClassRepository;
import club.ttg.dnd5.domain.character_class.rest.dto.ClassDetailedResponse;
import club.ttg.dnd5.domain.character_class.rest.dto.ClassRequest;
import club.ttg.dnd5.domain.character_class.rest.dto.ClassShortResponse;
import club.ttg.dnd5.domain.character_class.rest.mapper.ClassMapper;
import club.ttg.dnd5.domain.common.model.Gallery;
import club.ttg.dnd5.domain.common.model.SectionType;
import club.ttg.dnd5.domain.common.repository.GalleryRepository;
import club.ttg.dnd5.domain.common.rest.dto.SourceRequest;
import club.ttg.dnd5.exception.EntityExistException;
import club.ttg.dnd5.exception.EntityNotFoundException;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ClassService {

    private final ClassRepository classRepository;
    private final ClassMapper classMapper;
    private final ClassQueryDslSearchService classQueryDslSearchService;
    private final SourceService sourceService;
    private final GalleryRepository galleryRepository;
    private final SourceSavedFilterService sourceSavedFilterService;

    public List<ClassShortResponse> search(ClassQueryRequest request) {
        var predicate = ClassPredicateBuilder.build(request);
        return classQueryDslSearchService.search(predicate, request.getPage(), request.getPageSize())
                .stream()
                .filter(c -> (request.getSearch() != null && !request.getSearch().isEmpty()) || c.getParent() == null)
                .map(classMapper::toShort)
                .collect(Collectors.toList());
    }

    public boolean exists(String url) {
        return classRepository.existsById(url);
    }

    public CharacterClass findReferenceByUrl(String url) {
        return classRepository.getReferenceById(url);
    }

    public CharacterClass findByUrl(String url) {
        return classRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Класс с url %s не существует", url)));
    }

    @Transactional
    public ClassDetailedResponse save(ClassRequest request)
    {
        if (exists(request.getUrl()))
        {
            throw new EntityExistException(String.format("Класс с url %s уже существует", request.getUrl()));
        }

        CharacterClass toSave = classMapper.toEntity(request, getSource(request.getSource()));
        toSave.setParentUrl(request.getParentUrl());

        if (request.getParentUrl() != null)
        {
            CharacterClass parent = findByUrl(request.getParentUrl());

            if (CollectionUtils.isEmpty(toSave.getPrimaryCharacteristics()))
            {
                toSave.setPrimaryCharacteristics(parent.getPrimaryCharacteristics());
            }
            if (CollectionUtils.isEmpty(toSave.getSavingThrows()))
            {
                toSave.setSavingThrows(parent.getSavingThrows());
            }
            if (toSave.getSkillProficiency() == null)
            {
                toSave.setSkillProficiency(parent.getSkillProficiency());
            }
        }

        saveGallery(request.getUrl(), request.getGallery());
        CharacterClass saved = classRepository.save(toSave);
        return classMapper.toDetailedResponse(saved);
    }

    @Transactional
    @CacheEvict(cacheNames = "countAllMaterials")
    public void delete(String url) {
        CharacterClass characterClass = findByUrl(url);
        characterClass.setHiddenEntity(true);
        classRepository.save(characterClass);
    }

    @Transactional
    public String update(String url, ClassRequest request) {
        CharacterClass existingClass = findByUrl(url);

        if (!existingClass.getUrl().equals(request.getUrl())) {
            var saved = classMapper.toEntity(request, existingClass.getSource());
            saved.setParent(Optional.ofNullable(request.getParentUrl())
                    .map(this::findReferenceByUrl)
                    .orElse(null));
            classRepository.save(saved);
            classRepository.delete(existingClass);
        } else {
            if (request.getParentUrl() != null) {
                existingClass.setParent(findReferenceByUrl(request.getParentUrl()));
            }
            classMapper.updateEntity(existingClass,
                    request,
                    getSource(request.getSource())
            );
        }

        galleryRepository.deleteByUrlAndType(request.getUrl(), SectionType.CLASS);

        saveGallery(request.getUrl(), request.getGallery());
        return request.getUrl();
    }

    public List<ClassShortResponse> getSubclasses() {

        return classRepository.findAllByParentIsNotNull()
                .stream()
                .filter(characterClass -> !characterClass.isHiddenEntity())
                .map(classMapper::toShort)
                .toList();
    }

    public List<ClassShortResponse> getSubclasses(String parentUrl) {
        CharacterClass characterClass = classRepository.findByUrl(parentUrl)
                .orElseThrow(() -> new EntityNotFoundException("Класс не найден для URL:" + parentUrl));
        var sources = sourceSavedFilterService.getSavedSources();
        if (characterClass.isHiddenEntity()) {
            throw new EntityNotFoundException("Класс не найден для URL:" + parentUrl);
        }

        return characterClass.getSubclasses()
                .stream()
                .filter(subclass -> sources.contains(subclass.getSource().getAcronym()))
                .sorted(Comparator
                        .comparing((CharacterClass c) -> c.getSource().getType().ordinal())
                        .thenComparing(CharacterClass::getName)
                )
                .map(classMapper::toShort)
                .toList();
    }

    @Transactional(readOnly = true)
    public ClassDetailedResponse findDetailedByUrl(String url) {
        var charClass = findByUrl(url);
        var response = classMapper.toDetailedResponse(charClass);
        fillResponseFieldsFromParentClass(charClass, response);
        response.setGallery(galleryRepository.findAllByUrlAndType(url, SectionType.CLASS)
                .stream()
                .map(Gallery::getImage)
                .toList());
        return response;
    }

    public ClassRequest findFormByUrl(String url) {
        var request = classMapper.toRequest(findByUrl(url));
        request.setGallery(galleryRepository.findAllByUrlAndType(request.getUrl(), SectionType.CLASS)
                .stream()
                .map(Gallery::getImage)
                .toList());
        return request;
    }

    private void fillResponseFieldsFromParentClass(CharacterClass characterClass, ClassDetailedResponse response) {
        CharacterClass parent = characterClass.getParent();
        if (parent == null) {
            return;
        }

        if (response.getHitDice() == null && parent.getHitDice() != null) {
            response.setHitDice(classMapper.toDiceOptionDto(parent.getHitDice()));
        }

        if (!StringUtils.hasText(response.getPrimaryCharacteristics())) {
            response.setPrimaryCharacteristics(classMapper.toPrimaryCharacteristics(parent));
        }

        if (!StringUtils.hasText(response.getSavingThrows()) && parent.getSavingThrows() != null) {
            response.setSavingThrows(classMapper.toSavingThrowsString(parent.getSavingThrows()));
        }

        if (!StringUtils.hasText(response.getEquipment())) {
            response.setEquipment(parent.getEquipment());
        }

        if (response.getTable() == null) {
            response.setTable(parent.getTable());
        }

        if (response.getProficiency() == null) {
            response.setProficiency(new ClassProficiencyDto());
        }

        if (characterClass.getArmorProficiency() == null
                || CollectionUtils.isEmpty(characterClass.getArmorProficiency().getCategory())) {
            response.getProficiency().setArmor(classMapper.armorProficiencyToString(parent.getArmorProficiency()));
        }
        if (characterClass.getWeaponProficiency() == null
                || CollectionUtils.isEmpty(characterClass.getWeaponProficiency().getCategory())) {
            response.getProficiency().setWeapon(classMapper.weaponProficiencyToString(parent.getWeaponProficiency()));
        }
        if (characterClass.getSkillProficiency() == null
                || CollectionUtils.isEmpty(characterClass.getSkillProficiency().getSkills())) {
            response.getProficiency().setSkill(classMapper.skillProficiencyToString(parent.getSkillProficiency()));
        }
        if (!StringUtils.hasText(characterClass.getToolProficiency())) {
            response.getProficiency().setTool(parent.getToolProficiency());
        }
    }

    @Transactional(readOnly = true)
    public ClassDetailedResponse preview(ClassRequest request) {
        CharacterClass parent = Optional.ofNullable(request.getParentUrl())
                .map(this::findByUrl)
                .orElse(null);
        Source source = Optional.ofNullable(request.getSource())
                .map(SourceRequest::getUrl)
                .map(sourceService::findByUrl)
                .orElse(null);
        var entity = classMapper.toEntity(request, source);
        entity.setParent(parent);
        var response = classMapper.toDetailedResponse(entity);
        response.setGallery(galleryRepository.findAllByUrlAndType(response.getUrl(), SectionType.CLASS)
                .stream()
                .map(Gallery::getImage)
                .toList());
        return response;
    }

    @Transactional(readOnly = true)
    public Set<CharacterClass> findAllById(Set<String> urls)
    {
        if (urls == null || urls.isEmpty())
        {
            return Set.of();
        }
        return urls.stream()
                .map(classRepository::getReferenceById)
                .collect(Collectors.toSet());
    }

    @Transactional(readOnly = true)
    public List<CharacterClass> findAllMagicSubclasses() {
        return classRepository.findAllSubclassesWithSpellAffiliationAndCasterTypeNot(CasterType.NONE);
    }

    private Source getSource(SourceRequest source) {
        return Optional.ofNullable(source)
                .map(SourceRequest::getUrl)
                .map(sourceService::findByUrl)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<CharacterClass> findAllMagicClasses() {
        return classRepository.findAllByParentIsNullAndCasterTypeNot(CasterType.NONE);
    }

    private void saveGallery(String url, List<String> gallery) {
        if (!CollectionUtils.isEmpty(gallery)) {
            gallery.forEach(
                    image -> galleryRepository.save(Gallery.builder()
                            .url(url)
                            .type(SectionType.CLASS)
                            .image(image)
                            .build()));
        }
    }

    public List<ClassAbilityImprovementResponse> getAbilityImprovements() {
        return classRepository.findAllByParentIsNull(Sort.by("name")).stream()
                .map(classMapper::toAbilityResponse)
                .toList();
    }
}
