package club.ttg.dnd5.domain.character_class.service;

import club.ttg.dnd5.domain.book.model.Book;
import club.ttg.dnd5.domain.book.service.BookService;
import club.ttg.dnd5.domain.character_class.model.CharacterClass;
import club.ttg.dnd5.domain.character_class.repository.ClassRepository;
import club.ttg.dnd5.domain.character_class.rest.dto.ClassDetailedResponse;
import club.ttg.dnd5.domain.character_class.rest.dto.ClassRequest;
import club.ttg.dnd5.domain.character_class.rest.dto.ClassShortResponse;
import club.ttg.dnd5.domain.character_class.rest.mapper.ClassMapper;
import club.ttg.dnd5.domain.common.rest.dto.SourceRequest;
import club.ttg.dnd5.exception.EntityExistException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import club.ttg.dnd5.util.SwitchLayoutUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ClassService {

    private final ClassRepository classRepository;
    private final ClassMapper classMapper;
    private final BookService bookService;

    public List<ClassShortResponse> findAllClasses(String searchLine, String[] sort) {
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

        CharacterClass spell = classMapper.updateEntity(existingClass, parent, request, book);

        if (!Objects.equals(url, request.getUrl())) {
            classRepository.deleteById(url);
            classRepository.flush();
        }
        return classRepository.save(spell).getUrl();
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
        return classMapper.toDetailedResponse(findByUrl(url));
    }

    public ClassRequest findFormByUrl(String url) {
        return classMapper.toRequest(findByUrl(url));
    }

    public ClassDetailedResponse preview(ClassRequest request) {
        CharacterClass parent = Optional.ofNullable(request.getParentUrl())
                .map(this::findByUrl)
                .orElse(null);
        Book book = Optional.ofNullable(request.getSource())
                .map(SourceRequest::getUrl)
                .map(bookService::findByUrl)
                .orElse(null);
        return classMapper.toDetailedResponse(classMapper.toEntity(request, parent, book));
    }
}
