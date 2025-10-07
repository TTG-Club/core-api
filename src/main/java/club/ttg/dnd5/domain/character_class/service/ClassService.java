package club.ttg.dnd5.domain.character_class.service;

import club.ttg.dnd5.domain.book.model.Book;
import club.ttg.dnd5.domain.book.service.BookService;
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
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ClassService {

    private final ClassRepository classRepository;
    private final ClassMapper classMapper;
    private final BookService bookService;
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

    public ClassDetailedResponse save(ClassRequest request) {
        if (exists(request.getUrl())) {
            throw new EntityExistException(String.format("Класс с url %s уже существует", request.getUrl()));
        }

        CharacterClass parent = Optional.ofNullable(request.getParentUrl())
                .map(this::findByUrl)
                .orElse(null);

        Book book = Optional.ofNullable(request.getSource())
                .map(SourceRequest::getUrl)
                .map(bookService::findByUrl)
                .orElse(null);

        CharacterClass toSave = classMapper.toEntity(request, parent, book);
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

        Book book = Optional.ofNullable(request.getSource())
                .map(SourceRequest::getUrl)
                .map(bookService::findByUrl)
                .orElse(null);

        CharacterClass characterClass = classMapper.updateEntity(existingClass, parent, request, book);
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

        return characterClass.getSubclasses().stream().map(classMapper::toShortResponse).toList();
    }

    public ClassDetailedResponse findDetailedByUrl(String url) {
        var response = classMapper.toDetailedResponse(findByUrl(url));
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

    public ClassDetailedResponse preview(ClassRequest request) {
        CharacterClass parent = Optional.ofNullable(request.getParentUrl())
                .map(this::findByUrl)
                .orElse(null);
        Book book = Optional.ofNullable(request.getSource())
                .map(SourceRequest::getUrl)
                .map(bookService::findByUrl)
                .orElse(null);
        var response = classMapper.toDetailedResponse(classMapper.toEntity(request, parent, book));
        response.setGallery(galleryRepository.findAllByUrlAndType(response.getUrl(), SectionType.CLASS)
                .stream()
                .map(Gallery::getImage)
                .toList());
        return response;
    }

    public List<CharacterClass> findAllById(List<String> urls) {
        return classRepository.findAllById(urls);
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
