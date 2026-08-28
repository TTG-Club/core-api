package club.ttg.dnd5.domain.character_class.service;

import club.ttg.dnd5.domain.character_class.model.mechanics.ClassMechanics;
import club.ttg.dnd5.domain.character_class.rest.dto.ClassFeatureDto;
import club.ttg.dnd5.domain.common.rest.dto.GrantedSpellResponse;
import club.ttg.dnd5.domain.common.model.mechanics.SpellGrant;
import club.ttg.dnd5.domain.common.service.GrantedSpellResolver;
import java.util.Collection;
import java.util.Objects;
import club.ttg.dnd5.domain.character_class.rest.dto.ClassAbilityImprovementResponse;
import club.ttg.dnd5.domain.character_class.rest.dto.ClassProficiencyDto;
import club.ttg.dnd5.domain.character_class.rest.dto.ClassQueryRequest;
import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.source.service.SourceSavedFilterService;
import club.ttg.dnd5.domain.source.service.SourceService;
import club.ttg.dnd5.domain.character_class.model.CasterType;
import club.ttg.dnd5.domain.character_class.model.CharacterClass;
import club.ttg.dnd5.domain.character_class.model.ClassTableColumn;
import club.ttg.dnd5.domain.character_class.model.CounterTableColumns;
import club.ttg.dnd5.domain.character_class.repository.ClassRepository;
import club.ttg.dnd5.domain.character_class.rest.dto.ClassDetailedResponse;
import club.ttg.dnd5.domain.character_class.rest.dto.ClassRequest;
import club.ttg.dnd5.domain.character_class.rest.dto.ClassShortResponse;
import club.ttg.dnd5.domain.character_class.rest.mapper.ClassMapper;
import club.ttg.dnd5.domain.common.model.Gallery;
import club.ttg.dnd5.domain.common.model.SectionType;
import club.ttg.dnd5.domain.common.repository.GalleryRepository;
import club.ttg.dnd5.domain.common.rest.dto.SourceRequest;
import club.ttg.dnd5.domain.common.rest.mapper.EquipmentMapping;
import club.ttg.dnd5.domain.item.service.EquipmentNameResolver;
import club.ttg.dnd5.domain.revision.model.RevisionOperation;
import club.ttg.dnd5.domain.revision.service.EntityRevisionService;
import club.ttg.dnd5.exception.EntityExistException;
import club.ttg.dnd5.exception.EntityNotFoundException;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ClassService {

    public static final String REVISION_ENTITY_TYPE = "class";

    private final ClassRepository classRepository;
    private final ClassMapper classMapper;
    private final ClassQueryDslSearchService classQueryDslSearchService;
    private final SourceService sourceService;
    private final GalleryRepository galleryRepository;
    private final SourceSavedFilterService sourceSavedFilterService;
    private final EntityRevisionService revisionService;
    private final EquipmentNameResolver equipmentNameResolver;
    private final EquipmentMapping equipmentMapping;
    private final GrantedSpellResolver grantedSpellResolver;

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
        revisionService.record(REVISION_ENTITY_TYPE, saved.getUrl(), RevisionOperation.CREATE,
                findFormByUrl(saved.getUrl()));
        ClassDetailedResponse response = classMapper.toDetailedResponse(saved);
        fillCounterTableColumns(response);
        equipmentNameResolver.resolveNames(response.getStartingEquipment());
        return response;
    }

    @Transactional
    @CacheEvict(cacheNames = "countAllMaterials")
    public void delete(String url) {
        CharacterClass characterClass = findByUrl(url);
        characterClass.setHiddenEntity(true);
        classRepository.save(characterClass);
        revisionService.record(REVISION_ENTITY_TYPE, url, RevisionOperation.DELETE, findFormByUrl(url));
    }

    @Transactional
    public String update(String url, ClassRequest request) {
        CharacterClass existingClass = findByUrl(url);

        if (!existingClass.getUrl().equals(request.getUrl())) {
            if (exists(request.getUrl())) {
                throw new EntityExistException(String.format("Класс с url %s уже существует", request.getUrl()));
            }
            classRepository.delete(existingClass);
            classRepository.flush();
            var saved = classMapper.toEntity(request, getSource(request.getSource()));
            saved.setParentUrl(request.getParentUrl());
            classRepository.save(saved);
        } else {
            existingClass.setParentUrl(request.getParentUrl());
            classMapper.updateEntity(existingClass,
                    request,
                    getSource(request.getSource())
            );
        }

        galleryRepository.deleteByUrlAndType(url, SectionType.CLASS);

        saveGallery(request.getUrl(), request.getGallery());
        revisionService.record(REVISION_ENTITY_TYPE, request.getUrl(), RevisionOperation.UPDATE,
                findFormByUrl(request.getUrl()));
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
                // скрытые подклассы (мягкое удаление) в список не попадают — как и в общем списке
                .filter(subclass -> !subclass.isHiddenEntity())
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
        resolveFeatureGrantedSpells(response);
        fillCounterTableColumns(response);
        equipmentNameResolver.resolveNames(response.getStartingEquipment());
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

    /**
     * Подставляет умениям записи справочника по ссылкам их механики — одним запросом на
     * весь класс.
     *
     * <p>Ссылок в механике достаточно виртуальному столу, но не листу персонажа: чтобы
     * положить заклинание в книгу, ему нужен круг, а чтобы подписать — школа. Ненайденная
     * ссылка просто пропускается: это свободный JSONB, набранный руками в редакторе.</p>
     *
     * @param response деталь класса с уже собранными умениями.
     */
    private void resolveFeatureGrantedSpells(ClassDetailedResponse response) {
        var features = Optional.ofNullable(response.getFeatures()).orElse(List.of());

        var refs = features.stream()
                .map(ClassFeatureDto::getMechanics)
                .filter(Objects::nonNull)
                .map(ClassMechanics::getSpells)
                .filter(Objects::nonNull)
                .map(SpellGrant::getSpells)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .toList();

        if (refs.isEmpty()) {
            return;
        }

        var spellsByUrl = grantedSpellResolver.shortSpellsByUrl(refs);

        for (ClassFeatureDto feature : features) {
            var granted = Optional.ofNullable(feature.getMechanics())
                    .map(ClassMechanics::getSpells)
                    .map(SpellGrant::getSpells)
                    .orElse(List.of())
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(ref -> spellsByUrl.containsKey(ref.getUrl()))
                    .map(ref -> new GrantedSpellResponse(spellsByUrl.get(ref.getUrl()),
                            ref.getRequiredLevel()))
                    .toList();

            if (!granted.isEmpty()) {
                feature.setGrantedSpells(granted);
            }
        }
    }

    /**
     * Дописывает в таблицу прогрессии колонки ресурсов, отмеченных «показывать в
     * таблице».
     *
     * <p>Ряд по уровням у такого ресурса уже задан ступенями либо формулой, и второй раз
     * колонкой его не набирают. Колонки выводятся при отдаче, а не хранятся: иначе они
     * разошлись бы с ресурсом при первой же его правке.</p>
     *
     * @param response ответ записи класса.
     */
    private void fillCounterTableColumns(ClassDetailedResponse response) {
        List<CounterTableColumns.Source> sources = new ArrayList<>();
        if (response.getMechanics() != null) {
            sources.add(new CounterTableColumns.Source(response.getMechanics().getCounters(),
                    response.getMechanics().getChoices(), 1));
        }
        for (ClassFeatureDto feature : Optional.ofNullable(response.getFeatures()).orElse(List.of())) {
            if (feature != null && feature.getMechanics() != null) {
                sources.add(new CounterTableColumns.Source(feature.getMechanics().getCounters(),
                        feature.getMechanics().getChoices(), Math.max(1, feature.getLevel())));
            }
        }

        List<ClassTableColumn> table = CounterTableColumns.extend(response.getTable(), sources);
        response.setTable(table.isEmpty() ? response.getTable() : table);
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

        if (CollectionUtils.isEmpty(response.getStartingEquipment())) {
            response.setStartingEquipment(equipmentMapping.toEquipmentOptionDtos(parent.getStartingEquipment()));
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
        fillCounterTableColumns(response);
        equipmentNameResolver.resolveNames(response.getStartingEquipment());
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
