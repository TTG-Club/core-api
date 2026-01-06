package club.ttg.dnd5.domain.character_class.service;

import club.ttg.dnd5.domain.source.model.Source;
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
import club.ttg.dnd5.util.SwitchLayoutUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ClassService {

    private final ClassRepository classRepository;
    private final ClassMapper classMapper;
    private final SourceService sourceService;
    private final GalleryRepository galleryRepository;

    public List<ClassShortResponse> findAllClasses(String searchLine, String... sort) {
        Collection<CharacterClass> classes;

        if (StringUtils.hasText(searchLine)) {
            String invertedSearchLine = SwitchLayoutUtils.switchLayout(searchLine);
            classes = classRepository.findAllSearch(searchLine, invertedSearchLine, Sort.by(sort));
        } else {
            classes = classRepository.findAllByParentIsNull(Sort.by(sort));
        }

        return classes.stream()
                .map(classMapper::toShortResponse)
                .toList();
    }

    public boolean exists(String url) {
        return classRepository.existsById(url);
    }

    public CharacterClass findByUrl(String url) {
        return classRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Класс с url %s не существует", url)));
    }

    @Transactional
    public ClassDetailedResponse save(ClassRequest request) {
        if (exists(request.getUrl())) {
            throw new EntityExistException(String.format("Класс с url %s уже существует", request.getUrl()));
        }

        CharacterClass parent = Optional.ofNullable(request.getParentUrl())
                .map(this::findByUrl)
                .orElse(null);

        Source source = Optional.ofNullable(request.getSource())
                .map(SourceRequest::getUrl)
                .map(sourceService::findByUrl)
                .orElse(null);

        CharacterClass toSave = classMapper.toEntity(request, parent, source);
        saveGallery(request.getUrl(), request.getGallery());
        return classMapper.toDetailedResponse(classRepository.save(toSave));
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
        CharacterClass parent = null;

        if (request.getParentUrl() != null) {
            try {
                parent = findByUrl(request.getParentUrl());
            } catch (EntityNotFoundException e) {
                throw new EntityNotFoundException(String.format("Родительского класса с url %s не существует", request.getParentUrl()));
            }
        }

        Source source = Optional.ofNullable(request.getSource())
                .map(SourceRequest::getUrl)
                .map(sourceService::findByUrl)
                .orElse(null);

        CharacterClass characterClass = classMapper.updateEntity(existingClass, parent, request, source);
        if (!existingClass.getSubclasses().isEmpty()) {
            characterClass.setSubclasses(existingClass.getSubclasses());
            existingClass.setSubclasses(Collections.emptyList());
            classRepository.save(existingClass);
        }
        if (!Objects.equals(url, request.getUrl())) {
            classRepository.deleteById(url);
            classRepository.flush();
        }
        galleryRepository.deleteByUrlAndType(request.getUrl(), SectionType.CLASS);

        saveGallery(request.getUrl(), request.getGallery());
        return classRepository.save(characterClass).getUrl();
    }

    public List<ClassShortResponse> getSubclasses() {
        return classRepository.findAllByParentIsNotNull().stream()
                .filter(characterClass -> !characterClass.isHiddenEntity())
                .map(classMapper::toShortResponse)
                .toList();
    }

    public List<ClassShortResponse> getSubclasses(String parentUrl) {
        CharacterClass characterClass = classRepository.findByUrl(parentUrl)
                .orElseThrow(() -> new EntityNotFoundException("Класс не найден для URL:" + parentUrl));

        if (characterClass.isHiddenEntity()) {
            throw new EntityNotFoundException("Класс не найден для URL:" + parentUrl);
        }

        return characterClass.getSubclasses().stream()
                .sorted(Comparator
                        .comparing((CharacterClass c) -> c.getSource().getType().ordinal())
                        .thenComparing(CharacterClass::getName)
                )
                .map(classMapper::toShortResponse)
                .toList();
    }

    public ClassDetailedResponse findDetailedByUrl(String url) {
        var charClass = findByUrl(url);
        if (charClass.getParent() != null) {
            fillFieldFromParentClass(charClass);
        }
        var response = classMapper.toDetailedResponse(charClass);
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

    private void fillFieldFromParentClass(CharacterClass characterClass) {
        if (characterClass.getPrimaryCharacteristics() == null) {
            characterClass.setPrimaryCharacteristics(characterClass.getParent().getPrimaryCharacteristics());
        }
        if (characterClass.getSavingThrows() == null) {
            characterClass.setSavingThrows(characterClass.getParent().getSavingThrows());
        }
        if (characterClass.getHitDice() == null) {
            characterClass.setHitDice(characterClass.getParent().getHitDice());
        }
        if (characterClass.getEquipment() == null) {
            characterClass.setEquipment(characterClass.getParent().getEquipment());
        }
        if (characterClass.getArmorProficiency() == null) {
            characterClass.setArmorProficiency(characterClass.getParent().getArmorProficiency());
        }
        if (characterClass.getWeaponProficiency() == null) {
            characterClass.setWeaponProficiency(characterClass.getParent().getWeaponProficiency());
        }
        if (characterClass.getSkillProficiency() == null) {
            characterClass.setSkillProficiency(characterClass.getParent().getSkillProficiency());
        }
        if (characterClass.getToolProficiency() == null) {
            characterClass.setToolProficiency(characterClass.getParent().getToolProficiency());
        }
        if (characterClass.getTable() == null) {
            characterClass.setTable(characterClass.getParent().getTable());
        }
    }

    public ClassDetailedResponse preview(ClassRequest request) {
        CharacterClass parent = Optional.ofNullable(request.getParentUrl())
                .map(this::findByUrl)
                .orElse(null);
        Source source = Optional.ofNullable(request.getSource())
                .map(SourceRequest::getUrl)
                .map(sourceService::findByUrl)
                .orElse(null);
        var response = classMapper.toDetailedResponse(classMapper.toEntity(request, parent, source));
        response.setGallery(galleryRepository.findAllByUrlAndType(response.getUrl(), SectionType.CLASS)
                .stream()
                .map(Gallery::getImage)
                .toList());
        return response;
    }

    @Transactional(readOnly = true)
    public List<CharacterClass> findAllById(List<String> urls)
    {
        if (urls == null || urls.isEmpty())
        {
            return List.of();
        }
        return urls.stream()
                .map(classRepository::getReferenceById)
                .toList();
    }

    public List<ClassShortResponse> findAllMagicSubclasses() {
        return classRepository.findAllByParentIsNotNullAndCasterTypeNot(CasterType.NONE)
                .stream()
                .map(classMapper::toShortResponse)
        .toList();
    }

    public List<ClassShortResponse> findAllMagicClasses() {
        return classRepository.findAllByParentIsNullAndCasterTypeNot(CasterType.NONE)
                .stream()
                .map(classMapper::toShortResponse)
                .toList();
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
}
