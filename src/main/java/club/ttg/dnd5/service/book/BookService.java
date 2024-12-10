package club.ttg.dnd5.service.book;

import club.ttg.dnd5.dto.base.NameBasedDTO;
import club.ttg.dnd5.dto.base.TranslationDTO;
import club.ttg.dnd5.dto.book.SourceBookDTO;
import club.ttg.dnd5.exception.ApiException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import club.ttg.dnd5.model.base.Tag;
import club.ttg.dnd5.model.base.TagType;
import club.ttg.dnd5.model.base.Translation;
import club.ttg.dnd5.model.book.Book;
import club.ttg.dnd5.model.book.TypeBook;
import club.ttg.dnd5.repository.TagRepository;
import club.ttg.dnd5.repository.book.BookRepository;
import club.ttg.dnd5.service.TagService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    private final TagRepository tagRepository;
    private final TagService tagService;

    // Создание новой книги
    @Transactional
    public SourceBookDTO createBook(SourceBookDTO sourceBookDTO) {
        Book book = convertingCreateSourceToEntity(sourceBookDTO);
        Book savedBook = bookRepository.save(book);
        return convertingEntityToSourceDTO(savedBook);
    }

    // Получение книги по её sourceAcronym
    public Optional<SourceBookDTO> getBookBySourceAcronym(String sourceAcronym) {
        return bookRepository.findBySourceAcronym(sourceAcronym)
                .map(this::convertingEntityToSourceDTO);
    }

    // Поиск книги по типу
    public List<SourceBookDTO> getBooksByType(String type) {
        TypeBook bookType = TypeBook.valueOf(type.toUpperCase());
        return bookRepository.findByType(bookType).stream()
                .map(this::convertingEntityToSourceDTO)
                .collect(Collectors.toList());
    }

    // Получение всех типов книг
    public List<String> getAllBookTypes() {
        return Arrays.stream(TypeBook.values())
                .map(TypeBook::getName)
                .toList();
    }

    // Получение всех книг с определённым тегом
    public List<SourceBookDTO> getBooksByTag(String tagName) {
        Tag tag = tagRepository.findByNameIgnoreCase(tagName)
                .orElseThrow(() -> new EntityNotFoundException("Tag not found"));

        return bookRepository.findByTags(tag).stream()
                .map(this::convertingEntityToSourceDTO)
                .collect(Collectors.toList());
    }

    private Book convertingCreateSourceToEntity(SourceBookDTO sourceBookDTO) {
        NameBasedDTO name = sourceBookDTO.getName();
        if (StringUtils.isBlank(name.getShortName())) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Акроним у книги должен быть, это является ID, в бдшке");
        }
        return Book.builder()
                .year(sourceBookDTO.getYear())
                .sourceAcronym(name.getShortName())
                .name(name.getName())
                .englishName(name.getEnglish())
                .authors(sourceBookDTO.getAuthor())
                .image(sourceBookDTO.getImage())
                .description(sourceBookDTO.getDescription())
                .tags(generatingTags(sourceBookDTO.getTags()))
                .type(TypeBook.valueOf(sourceBookDTO.getType()))
                .translation(convertingTranslation(sourceBookDTO.getTranslation()))
                .build();
    }

    private Set<Tag> generatingTags(Set<String> tags) {
        return tags
                .stream()
                .map(tagName ->
                        new Tag(tagName, TagType.TAG_BOOK))
                .collect(Collectors.toSet());
    }

    private Translation convertingTranslation(TranslationDTO translationDTO) {
        return Translation.
                builder()
                .translationYear(translationDTO.getYear())
                .authors(translationDTO.getAuthor())
                .build();
    }

    private SourceBookDTO convertingEntityToSourceDTO(Book book) {
        if (book == null || StringUtils.isBlank(book.getSourceAcronym())) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Сущность книги невалидная");
        }

        return SourceBookDTO.builder()
                .year(book.getYear())
                .name(NameBasedDTO.builder()
                        .shortName(book.getSourceAcronym())
                        .name(book.getName())
                        .english(book.getEnglishName())
                        .build())
                .author(new HashSet<>(book.getAuthors()))
                .image(book.getImage())
                .description(book.getDescription())
                .tags(book.getTags().stream()
                        .map(Tag::getName)
                        .collect(Collectors.toSet()))
                .type(book.getType().name())
                .translation(convertingTranslationToDTO(book.getTranslation()))
                .build();
    }

    private TranslationDTO convertingTranslationToDTO(Translation translation) {
        if (translation == null) {
            return null;
        }

        return TranslationDTO.builder()
                .year(translation.getTranslationYear())
                .author(new HashSet<>(translation.getAuthors()))
                .build();
    }
}
